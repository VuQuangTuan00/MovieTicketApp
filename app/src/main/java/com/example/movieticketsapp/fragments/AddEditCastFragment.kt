import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.FragmentAddEditCastLayoutBinding
import com.example.movieticketsapp.model.Cast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch

class AddEditCastFragment : Fragment() {

    private var _binding: FragmentAddEditCastLayoutBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null
    private var isEditMode = false
    private var castId: String? = null
    private var existingAvatarUrl: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .into(binding.imgViewAvatar)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditCastLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isEditMode = arguments?.getBoolean("isEditMode") ?: false
        castId = arguments?.getString("castId")

        if (isEditMode && castId != null) {
            loadCastInfo(castId!!)
        }

        binding.imgViewAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val name = binding.edtCastName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode && castId != null) {
                val castMovie = Cast(castId!!, "", name)
                uploadAvatarAndSaveToFirestore(castMovie, isNew = false)
            } else {
                uploadAvatarAndSaveToFirestore(null, isNew = true, name)
            }
        }

        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun loadCastInfo(castId: String) {
        db.collection("cast").document(castId).get()
            .addOnSuccessListener { document ->
                val cast = document.toObject(Cast::class.java)
                cast?.let {
                    binding.edtCastName.setText(it.name)
                    existingAvatarUrl = it.avatar
                    Glide.with(requireContext()).load(it.avatar).into(binding.imgViewAvatar)
                }
            }
    }

    private fun uploadAvatarAndSaveToFirestore(castMovie: Cast?, isNew: Boolean, newName: String = "") {
        val castRef = if (isNew) db.collection("cast").document() else db.collection("cast").document(castMovie!!.id)
        val name = if (isNew) newName else castMovie!!.name
        //val initialAvatar = if (isNew) "" else castMovie!!.avatar
        val initialAvatar = if (isNew) "" else existingAvatarUrl ?: ""

        val cast = Cast(castRef.id, initialAvatar, name)

        castRef.set(cast)
            .addOnSuccessListener {
                if (selectedImageUri != null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val ctx = context ?: return@launch
                            val tempFile = File(ctx.cacheDir, "temp_image.jpg").apply {
                                ctx.contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                                    outputStream().use { output -> input.copyTo(output) }
                                }
                            }

                            val compressedFile = Compressor.compress(ctx, tempFile) {
                                quality(50)
                                format(Bitmap.CompressFormat.JPEG)
                            }

                            val imageFileName = "${UUID.randomUUID()}_${name.replace(" ", "_")}.jpg"
                            val storageRef = FirebaseStorage.getInstance().reference.child("cast_avatars/$imageFileName")

                            storageRef.putFile(Uri.fromFile(compressedFile))
                                .addOnSuccessListener {
                                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                                        castRef.update("avatar", uri.toString())

                                        Toast.makeText(context, if (isNew) "Thêm diễn viên thành công" else "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                        parentFragmentManager.setFragmentResult("update_cast_result", Bundle().apply {
                                            putBoolean("isUpdated", true)
                                        })
                                        requireActivity().onBackPressed()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Lỗi tải ảnh: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi nén ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, if (isNew) "Thêm diễn viên thành công" else "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult("update_cast_result", Bundle().apply {
                        putBoolean("isUpdated", true)
                    })
                    requireActivity().onBackPressed()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi lưu thông tin: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

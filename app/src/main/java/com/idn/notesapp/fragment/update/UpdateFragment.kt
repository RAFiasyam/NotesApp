package com.idn.notesapp.fragment.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.idn.notesapp.R
import com.idn.notesapp.data.model.NoteData
import com.idn.notesapp.data.viewModelData.NotesViewModel
import com.idn.notesapp.databinding.FragmentAddBinding
import com.idn.notesapp.databinding.FragmentUpdate2Binding
import com.idn.notesapp.fragment.SharedViewModels

class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private val mSharedViewModel : SharedViewModels by viewModels()
    private val mNotesViewModel : NotesViewModel by viewModels()

    private var _binding : FragmentUpdate2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdate2Binding.inflate(inflater, container, false)
        binding.args = args

        binding.spUpdate.onItemSelectedListener = mSharedViewModel.listener

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
 //       super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmItemRemoval() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete '${args.currentItem.title}'?")
            .setMessage("Are You Sure Want To Remove'${args.currentItem.title}'?")
            .setPositiveButton("Yes"){_,_ ->
                Toast.makeText(requireContext(), "Successfully Removed : ${args.currentItem.title}"
                    , Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_updateFragment2_to_listFragment)
            }
            .setNegativeButton("No"){_,_ -> }
            .create()
            .show()
    }

    private fun updateItem() {
        var title = binding.etUptitle.text.toString()
        val description = binding.etDescUpdate.text.toString()
        val getPriority = binding.spUpdate.selectedItem.toString()

        val validation  = mSharedViewModel.verifyDataFromUser(title, description)
        if(validation){
            val updateitem = NoteData(
                args.currentItem.id,
                title,
                mSharedViewModel.parsePriority(getPriority),
                description
            )
            mNotesViewModel.updateData(updateitem)
            Toast.makeText(requireContext(), "Berhasil Di Update", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment2_to_listFragment)
        }else{
            Toast.makeText(requireContext(), "Tolong Isi Semua Persyaratan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
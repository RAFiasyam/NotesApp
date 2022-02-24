package com.idn.notesapp.fragment.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.idn.notesapp.R
import com.idn.notesapp.Utils.hideKeyboard
import com.idn.notesapp.data.model.NoteData
import com.idn.notesapp.data.viewModelData.NotesViewModel
import com.idn.notesapp.databinding.FragmentListBinding
import com.idn.notesapp.fragment.SharedViewModels

import com.idn.notesapp.fragment.list.Adapter.ListAdapter
import jp.wasabeef.recyclerview.animators.LandingAnimator

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mNotesViewModel : NotesViewModel by viewModels()
    private val adapter : ListAdapter by lazy { ListAdapter() }
    private val mSharedViewModel : SharedViewModels by viewModels()
    private var _listBinding : FragmentListBinding? = null
    private val listBinding get() = _listBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _listBinding = FragmentListBinding.inflate(inflater, container, false)
        listBinding.lifecycleOwner = this
        listBinding.mSharedViewModel = mSharedViewModel

        setUpRecyclerview()

        mNotesViewModel.getAllData.observe(viewLifecycleOwner, { data ->
            mSharedViewModel.checkDatabaseEmpty(data)
            adapter.setData(data)
        })

        setHasOptionsMenu(true)
        hideKeyboard(requireActivity())
        return listBinding.root
    }

    private fun setUpRecyclerview() {
        //set untuk atur recycerview
        listBinding.rvList.adapter = adapter
        listBinding.rvList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        listBinding.rvList.itemAnimator = LandingAnimator().apply {
            addDuration = 300
        }
        // swipe to delete
        swipeToDelete(listBinding.rvList)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deleteItem = adapter.dataList[viewHolder.adapterPosition]
                mNotesViewModel.deleteData(deleteItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                restoreDeleteData(viewHolder.itemView, deleteItem)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private  fun restoreDeleteData(view: View, deleteItem: NoteData) {
        val snackbar = Snackbar.make(
            view,"Delete '${deleteItem.title}'",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Undo") {
            mNotesViewModel.insertData(deleteItem)
        }
        snackbar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete_all -> confirmRemoveAll()
            R.id.menu_priority_high -> mNotesViewModel.sortByHighPriority.observe(this, {
                adapter.setData(it)
            })
            R.id.menu_priority_low -> mNotesViewModel.sortByLowPriority.observe(this, {
                adapter.setData(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmRemoveAll() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All ?")
            .setMessage("Are You Sure want to Remove All ?")
            .setPositiveButton("Yes"){_,_->
                mNotesViewModel.deleteAllData()
                Toast.makeText(requireContext(), "Successfuly Remove All",
                    Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null){
            searchThroughtDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if(query != null){
            searchThroughtDatabase(query)
        }
        return true
    }

    private fun searchThroughtDatabase(query: String?){
        val searchQuery = "%$query%"

        mNotesViewModel.searchDatabase(searchQuery).observe(this, {
            list -> list.let { adapter.setData(it)}
        })
    }

    override fun onDestroy() {
        _listBinding = null
        super.onDestroy()
    }

}
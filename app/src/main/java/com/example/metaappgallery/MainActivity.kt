package com.example.metaappgallery

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chad.library.adapter.base.loadmore.LoadMoreStatus
import com.example.metaappgallery.api.GalleryApi
import com.example.metaappgallery.databinding.ActivityMainBinding
import com.example.metaappgallery.repository.GalleryRepository
import com.example.metaappgallery.ui.GalleryAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: GalleryViewModel

    private var searchView: SearchView? = null
    private var toggleView: MenuItem? = null

    companion object {
        const val DEFAULT_SEARCH_HINT = "北京"
        const val SPAN_COUNT = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val netService = GalleryApi.create()
        val repository = GalleryRepository(netService)
        viewModel = ViewModelProviders
            .of(this, GalleryViewModel.FACTORY(repository))
            .get(GalleryViewModel::class.java)
            .also { it.refresh(DEFAULT_SEARCH_HINT) }

        val staggeredGridLayoutManager = StaggeredGridLayoutManager(
            SPAN_COUNT,
            StaggeredGridLayoutManager.VERTICAL
        )
        val gridLayoutManager = GridLayoutManager(this, SPAN_COUNT)

        val itemWidth = resources.displayMetrics.widthPixels / SPAN_COUNT
        val adapter = GalleryAdapter.create(itemWidth)
        adapter.loadMoreModule.setOnLoadMoreListener { viewModel.loadMore() }
        binding.galleryList.layoutManager = gridLayoutManager
        binding.galleryList.adapter = adapter
        binding.statusLayout.onRetry = { viewModel.refresh() }
        binding.refreshLayout.setOnRefreshListener { viewModel.refresh() }

        viewModel.layoutStatus.observe(this, Observer {
            binding.statusLayout.setStatus(it)
        })
        viewModel.refreshLayoutShown.observe(
            this,
            Observer { binding.refreshLayout.isRefreshing = it })
        viewModel.loadMoreStatus.observe(this, Observer {
            when (it) {
                LoadMoreStatus.Complete -> adapter.loadMoreModule.loadMoreComplete()
                LoadMoreStatus.Fail -> adapter.loadMoreModule.loadMoreFail()
                LoadMoreStatus.End -> adapter.loadMoreModule.loadMoreEnd()
                else -> adapter.loadMoreModule.loadMoreToLoading()
            }
        })
        viewModel.data.observe(this, Observer {
            adapter.setNewInstance(it)
        })
        viewModel.isStaggeredGrid.observe(this, Observer {
            val lastPosition = when (val currentManager = binding.galleryList.layoutManager) {
                is GridLayoutManager -> {
                    currentManager.findFirstVisibleItemPosition()
                }
                is StaggeredGridLayoutManager -> {
                    currentManager.findFirstVisibleItemPositions(IntArray(SPAN_COUNT))[0]
                }
                else -> 0
            }
            binding.galleryList.layoutManager =
                if (it) staggeredGridLayoutManager else gridLayoutManager
            adapter.isStaggeredGridItem = it
            adapter.notifyDataSetChanged()
            binding.galleryList.scrollToPosition(lastPosition)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        searchView = menu.findItem(R.id.actionSearch).actionView as SearchView
        toggleView = menu.findItem(R.id.actionToggleLayout)

        searchView?.clearFocus()
        searchView?.queryHint = getString(R.string.search_hint)
        searchView?.setIconifiedByDefault(true)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val keywords = if (query.isEmpty()) searchView?.queryHint ?: "" else query
                viewModel.refresh(keywords.toString())
                searchView?.onActionViewCollapsed()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        toggleView?.setOnMenuItemClickListener {
            viewModel.toggleLayout()
            true
        }
        viewModel.isStaggeredGrid.observe(this, Observer {
            toggleView?.setIcon(
                if (it) {
                    R.drawable.ic_staggered_grid
                } else {
                    R.drawable.ic_grid
                }
            )
        })

        return super.onCreateOptionsMenu(menu)
    }
}
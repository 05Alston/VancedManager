package com.vanced.manager.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.crowdin.platform.util.inflateWithCrowdin
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.vanced.manager.R
import com.vanced.manager.adapter.ExpandableAppListAdapter
import com.vanced.manager.adapter.LinkAdapter
import com.vanced.manager.adapter.SponsorAdapter
import com.vanced.manager.core.ui.base.BindingFragment
import com.vanced.manager.databinding.FragmentHomeBinding
import com.vanced.manager.ui.dialogs.DialogContainer.installAlertBuilder
import com.vanced.manager.ui.viewmodels.HomeViewModel
import com.vanced.manager.ui.viewmodels.HomeViewModelFactory
import com.vanced.manager.utils.isFetching

class HomeFragment : BindingFragment<FragmentHomeBinding>() {

    companion object {
        const val INSTALL_FAILED = "INSTALL_FAILED"
        const val REFRESH_HOME = "REFRESH_HOME"
    }

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireActivity())
    }

    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(requireActivity()) }

    override fun binding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun otherSetups() {
        bindData()
    }

    private fun bindData() {
        requireActivity().title = getString(R.string.title_home)
        setHasOptionsMenu(true)
        with (binding) {
            homeRefresh.setOnRefreshListener { viewModel.fetchData() }
            isFetching.observe(viewLifecycleOwner) { homeRefresh.isRefreshing = it }

            recyclerAppList.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = ExpandableAppListAdapter(requireActivity(), viewModel /*, tooltip*/ )
                setHasFixedSize(true)
            }

            recyclerSponsors.apply {
                val lm = FlexboxLayoutManager(requireActivity())
                lm.justifyContent = JustifyContent.SPACE_EVENLY
                layoutManager = lm
                setHasFixedSize(true)
                adapter = SponsorAdapter(requireActivity(), viewModel)
            }

            recyclerLinks.apply {
                val lm = FlexboxLayoutManager(requireActivity())
                lm.justifyContent = JustifyContent.SPACE_EVENLY
                layoutManager = lm
                setHasFixedSize(true)
                adapter = LinkAdapter(requireActivity(), viewModel)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflateWithCrowdin(R.menu.toolbar_menu, menu, resources)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPause() {
        super.onPause()
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        registerReceivers()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                INSTALL_FAILED -> installAlertBuilder(intent.getStringExtra("errorMsg").toString(), intent.getStringExtra("fullErrorMsg"), requireActivity())
                REFRESH_HOME -> viewModel.fetchData()
            }
        }
    }

    private fun registerReceivers() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(INSTALL_FAILED)
        intentFilter.addAction(REFRESH_HOME)
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }
}


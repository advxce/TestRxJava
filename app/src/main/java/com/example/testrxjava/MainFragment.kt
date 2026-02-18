package com.example.testrxjava

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testrxjava.databinding.FragmentMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    val api = RetrofitClient.api
    private val compositeDisposable = CompositeDisposable()
    var customAdapter: CustomAdapter? = null
    val itemPositionSubject = PublishSubject.create<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initData()
        initRecView()
        initTextView()
        initToast()

    }

    private fun initAdapter() {
        customAdapter = CustomAdapter(){
            itemPositionSubject.onNext(it)
        }
    }


    private fun initData() {
        val data = api.loadItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { newList ->
                    customAdapter?.updateList(newList)
                },
                {
                    println(it.message)
                }
            )

        compositeDisposable.add(data)
    }

    private fun initRecView() {
        with(binding) {
            recView.adapter = customAdapter
            recView.layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    private fun initTextView(){
        with(binding){

            val timer = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    tv1.text = it.toString()
                }
            compositeDisposable.add(timer)
        }
    }

    private fun initToast(){
        val disposableItemPosition = itemPositionSubject
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
            Toast.makeText(requireActivity(), it.toString(), Toast.LENGTH_SHORT).show()
        }
        compositeDisposable.add(disposableItemPosition)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        customAdapter = null
        _binding = null
    }
}
package com.example.testrxjava

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testrxjava.databinding.FragmentMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import okio.IOException
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
        initEditText()
        taskWithTwoServersA()
        taskWithTwoServersB()

    }

    private fun firstServer(): Single<List<SomeCard>> =
        Single.fromCallable {
            listOf(
                SomeCard(1, "Карта 1", 5),
                SomeCard(2, "Карта 2", 10),
            )
        }.subscribeOn(Schedulers.io())

    private fun secondServer(): Single<List<SomeCard>> =
        Single.error<List<SomeCard>>(IOException("rre"))
            .subscribeOn(Schedulers.io())


    private fun taskWithTwoServersA() {
        val request1 = firstServer()
            .onErrorReturn { emptyList() }
        val request2 = secondServer()
            .onErrorReturn { emptyList() }

        val result = Single.zip(request1, request2) { list1, list2 ->
            list1 + list2
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { cards ->
                    println("all cards $cards")
                },
                { error ->
                    println("error: ${error.message}")
                }
            )

        compositeDisposable.add(result)

    }

    private fun taskWithTwoServersB() {
        val request1 = firstServer()
            .toObservable()
            .onErrorComplete()
        val request2 = secondServer()
            .toObservable()
            .onErrorComplete()

        val result = Observable.zip(request1, request2) { list1, list2 ->
            list1 + list2
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { cards ->
                    println("all cards $cards")
                },
                { error ->
                    println("error: ${error.message}")

                }
            )

        compositeDisposable.add(result)

    }


    private fun initEditText() {
        val subject = PublishSubject.create<String>()
        with(binding) {

            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    subject.onNext(p0.toString())
                }

                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

            })
        }

        val editTextDisposable = subject
            .debounce(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.i("CheckEdit", it)
            }

        compositeDisposable.add(editTextDisposable)
    }

    private fun initAdapter() {
        customAdapter = CustomAdapter() {
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

    private fun initTextView() {
        with(binding) {

            val timer = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    tv1.text = it.toString()
                }
            compositeDisposable.add(timer)
        }
    }

    private fun initToast() {
        val disposableItemPosition = itemPositionSubject
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
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
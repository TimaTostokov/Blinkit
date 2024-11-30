package com.example.blinkit.core.common

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class ViewBindingDelegate<T : ViewBinding>(
    private val bindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null
    private var isRegistered = false

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            if (f == this@ViewBindingDelegate.thisRef) {
                binding = null
                if (isRegistered) {
                    this@ViewBindingDelegate.thisRef.parentFragmentManager.unregisterFragmentLifecycleCallbacks(this)
                    isRegistered = false
                }
            }
            super.onFragmentViewDestroyed(fm, f)
        }
    }

    private lateinit var thisRef: Fragment

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        this.thisRef = thisRef

        val lifecycle = thisRef.viewLifecycleOwner.lifecycle
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            binding?.let { return it }
            if (!isRegistered) {
                thisRef.parentFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
                isRegistered = true
            }
            binding = bindingFactory(thisRef.requireView())
            return binding!!
        } else {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(factory: (View) -> T) =
    ViewBindingDelegate(factory)
package com.fuck.iab

import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import java.lang.reflect.Method
import java.lang.reflect.Member

private data class HookData(
    val before: ((XposedInterface.BeforeHookCallback) -> Unit)?,
    val after: ((XposedInterface.AfterHookCallback) -> Unit)?
)

private val hookCallbacks = mutableMapOf<Member, HookData>()

@XposedHooker
private class HookCallback : XposedInterface.Hooker {

    companion object {

        @JvmStatic
        @BeforeInvocation
        fun before(param: XposedInterface.BeforeHookCallback) {
            hookCallbacks[param.member]?.before?.invoke(param)
        }

        @JvmStatic
        @AfterInvocation
        fun after(param: XposedInterface.AfterHookCallback) {
            hookCallbacks[param.member]?.after?.invoke(param)
        }
    }
}


class HookBuilder {

    internal var before: ((XposedInterface.BeforeHookCallback) -> Unit)? = null
    internal var after: ((XposedInterface.AfterHookCallback) -> Unit)? = null

    fun before(block: XposedInterface.BeforeHookCallback.() -> Unit) {
        before = {
            it.block()
        }
    }

    fun after(block: XposedInterface.AfterHookCallback.() -> Unit) {
        after = {
            it.block()
        }
    }
}


fun XposedInterface.hook(
    method: Method,
    block: HookBuilder.() -> Unit
) {
    val builder = HookBuilder()
    builder.block()

    hookCallbacks[method] = HookData(
        builder.before,
        builder.after
    )

    hook(method, HookCallback::class.java)
}
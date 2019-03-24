package me.nathan3882.howfast;

import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

public interface IActivityReferencer<T> {

    WeakReference<T> getWeakReference();

    @Nullable
    default T getReferenceValue() {
        return getWeakReference().get();
    }
}

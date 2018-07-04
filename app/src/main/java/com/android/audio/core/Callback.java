package com.android.audio.core;

public interface Callback {
    void onBufferAvailable(byte[] buffer);
}
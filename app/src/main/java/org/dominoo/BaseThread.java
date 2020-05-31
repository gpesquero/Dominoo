package org.dominoo;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseThread extends Thread {

    // Handler to send messages to the listener
    private Handler mHandler = null;

    // Input event semaphore
    private Semaphore mInputEventSemaphore = new Semaphore(0);

    // Input
    private ReentrantLock mInputEventLock = new ReentrantLock();

    // Input event queue
    private ArrayList<ThreadEvent> mInputEventQueue = new ArrayList<ThreadEvent>();

    private ReentrantLock mOutputEventLock = new ReentrantLock();

    // Output event queue
    private ArrayList<ThreadEvent> mOutputEventQueue = new ArrayList<ThreadEvent>();

    /*
    public interface ThreadEventListener {



    }
    */

    // run() function to be instanced by subclasses
    public abstract void run();

    // dispatchEvent() function to be instanced by subclasses
    protected abstract void dispatchEvent(ThreadEvent event);

    BaseThread() {

        /*
        mHandler = new Handler();

        if (Looper.myLooper() == Looper.getMainLooper()) {

            mHandler = new Handler();
        }
        */
    }

    protected void createHandler() {

        mOutputEventLock.lock();

        mHandler = new Handler();

        dispatchOutputEvents();

        mOutputEventLock.unlock();
    }

    protected void addInputEvent(ThreadEvent threadEvent) {

        mInputEventLock.lock();

        mInputEventQueue.add(threadEvent);

        mInputEventLock.unlock();

        mInputEventSemaphore.release();
    }

    protected ThreadEvent waitForInputEvent() throws InterruptedException {

        mInputEventSemaphore.acquire();

        mInputEventLock.lock();

        ThreadEvent event = mInputEventQueue.remove(0);

        mInputEventLock.unlock();

        return event;
    }

    protected void addOutputEvent(ThreadEvent threadEvent) {

        mOutputEventLock.lock();

        mOutputEventQueue.add(threadEvent);

        dispatchOutputEvents();

        mOutputEventLock.unlock();

        /*
        if (mListener != null) {

            sendEventsToListener();
        }
        */

        //mSemaphore.release();
    }

    private void dispatchOutputEvents() {

        if (mHandler == null) {

            // No handler is set
            return;
        }

        while (mOutputEventQueue.size() > 0) {

            final ThreadEvent event = mOutputEventQueue.remove(0);

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    dispatchEvent(event);
                }
            });
        }
    }
}

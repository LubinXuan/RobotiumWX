package me.robin.espressomodule;

/**
 * Created by xuanlubin on 2017/4/26.
 */
public abstract class Promise {
    private Promise success;

    private Promise failure;

    public abstract void service();

    public final void run() {
        try {
            this.service();
            if (null != this.success) {
                success.run();
            }
        } catch (Exception e) {
            if (null != this.failure) {
                failure.run();
            }
        }
    }

    public Promise withSuccess(Promise promise) {
        this.success = promise;
        return this;
    }

    public Promise withFailure(Promise promise) {
        this.failure = promise;
        return this;
    }
}

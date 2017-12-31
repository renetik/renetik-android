package cs.android.rpc;

import cs.java.collections.CSList;

import static cs.java.lang.CSLang.doLater;
import static cs.java.lang.CSLang.empty;
import static cs.java.lang.CSLang.is;
import static cs.java.lang.CSLang.list;

public class CSConcurrentResponse<T> extends CSResponse<T> {

    private CSList<CSResponse> _responses = list();
    private CSList<CSResponse> _failedResponses = list();
    private CSResponse<T> _mainResponse;

    public CSConcurrentResponse() {
    }

    public CSConcurrentResponse(String title) {
        title(title);
    }

    public CSConcurrentResponse(CSResponse<T> response, CSResponse<?>... responses) {
        addMain(response);
        addAll(responses);
    }

    private CSResponse<T> addMain(CSResponse<T> response) {
        _mainResponse = response;
        return add(response);
    }

    public <Data> CSResponse<Data> add(final CSResponse<Data> response) {
        if (response.isSending()) onResponseSend();
        _responses.add(response);
        return response.
                onSend(() -> onResponseSend()).
                onSuccess(() -> onResponseSuccess(response)).
                onFailed(failedResponse -> onResponseFailed(failedResponse));
    }

    private void onResponseSend() {
        if (!isSending()) sending(this);
    }

    private void onResponseFailed(CSResponse failedResponse) {
        _failedResponses.add(failedResponse);
        _responses.delete(failedResponse);
        if (empty(_responses)) onResponsesDone();
    }

    private void onResponseSuccess(CSResponse response) {
        _responses.delete(response);
        if (empty(_responses)) onResponsesDone();
    }

    public CSConcurrentResponse<T> addAll(CSResponse<?>... responses) {
        for (CSResponse<?> response : responses) add(response);
        return this;
    }

    public void cancel() {
        super.cancel();
        for (CSResponse response : _responses) response.cancel();
    }

    public CSResponse<?> resend() {
        reset();
        for (CSResponse response : _responses) response.resend();
        return this;
    }

    public void onResponsesDone() {
        if (_failedResponses.hasItems()) failed(this);
        else if (is(_mainResponse)) success(_mainResponse.data());
        else super.success();
    }

    public void reset() {
        super.reset();
        _responses.clear();
        _failedResponses.clear();
    }

    public void onAddDone() {
        if (empty(_responses)) doLater(this::onResponsesDone);
    }

}

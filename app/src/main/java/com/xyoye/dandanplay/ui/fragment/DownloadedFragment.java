package com.xyoye.dandanplay.ui.fragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.mvp.impl.DownloadedFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadedFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadedFragmentView;
import com.xyoye.dandanplay.ui.weight.item.DownloadedTaskItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadedFragment extends BaseMvpFragment<DownloadedFragmentPresenter> implements DownloadedFragmentView {

    @BindView(R.id.task_rv)
    RecyclerView taskRv;

    private BaseRvAdapter<DownloadedTaskBean> taskRvAdapter;
    private List<DownloadedTaskBean> taskList;

    public static DownloadedFragment newInstance() {
        return new DownloadedFragment();
    }

    @NonNull
    @Override
    protected DownloadedFragmentPresenter initPresenter() {
        return new DownloadedFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_downloaded;
    }

    @Override
    public void initView() {
        taskList = new ArrayList<>();
        taskRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        taskRvAdapter = new BaseRvAdapter<DownloadedTaskBean>(taskList) {
            @NonNull
            @Override
            public AdapterItem<DownloadedTaskBean> onCreateItem(int viewType) {
                return new DownloadedTaskItem();
            }
        };
        taskRv.setAdapter(taskRvAdapter);

        presenter.queryDownloadedTask();
    }

    @Override
    public void initListener() {

    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {

    }

    @Override
    public void updateTask(List<DownloadedTaskBean> taskList) {
        this.taskList.clear();
        this.taskList.addAll(taskList);
        taskRvAdapter.notifyDataSetChanged();
    }

    public void updateTask(){
        presenter.queryDownloadedTask();
    }
}
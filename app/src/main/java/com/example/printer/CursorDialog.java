package com.example.printer;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;


public class CursorDialog extends Dialog {

    public CursorDialog(Context context) {
        super(context);
    }

    public CursorDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {
        private final Context mContext;

        //设置参数
        //dialog是否全屏、默认不全屏
        private boolean isFloating = true;//默认false 浮动  窗口化
        private String title;//默认false
        private boolean isFull;//全屏

        private View layout;

        //Dialog布局
        public Builder setlayout(int res) {
            layout = View.inflate(mContext, res, null);
            return this;
        }

        public Builder setView(View v) {
            this.layout = v;
            return this;
        }

        //布局设置点击事件
        public Builder setViewClick(int id, View.OnClickListener listener) {
            View view = layout.findViewById(id);
            view.setOnClickListener(listener);
            return this;
        }

        //Dialog布局中有ListView,比如弹出让选择一个省份
        public Builder setViewClick(int id, AdapterView.OnItemClickListener listener) {
            View view = layout.findViewById(id);
            if (view instanceof AdapterView) {
                AdapterView v = (AdapterView) view;
                v.setOnItemClickListener(listener);
            }

            return this;
        }


        public Builder notFloating() {
            isFloating = false;
            return this;
        }


        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder full() {
            isFull = true;
            isFloating = false;
            return this;
        }

        public Builder(Context context) {
            this.mContext = context;
        }

        //Dialog主题样式
        public CursorDialog build() {
            CursorDialog dialog = null;
            //根据样式参数，加载不同主题
            if (isFloating) {
                //浮动的
                if (TextUtils.isEmpty(title)) {
                    //无title
                    dialog = new CursorDialog(mContext, R.style.CursorDialogThemeNoTitle);
                } else {
                    //有title
                    dialog = new CursorDialog(mContext);
                }
            } else {
                //不浮动
                if (TextUtils.isEmpty(title)) {
                    if (isFull) {
                        //全屏
                        dialog = new CursorDialog(mContext, R.style.CursorDialogNoTitleFullTheme);

                    } else {
                        //非全屏
                        dialog = new CursorDialog(mContext, R.style.CursorDialogNotFloatNoTitleTheme);
                    }
                } else {
                    if (isFull) {
                        //全屏
                        dialog = new CursorDialog(mContext, R.style.CursorDialogFullTheme);
                    } else {
                        //非全屏
                        dialog = new CursorDialog(mContext, R.style.CursorDialogThemeNotFloat);
                    }
                }
            }

            if (layout == null)
                throw new IllegalArgumentException("View can not be empty.");
            dialog.setContentView(layout);
            return dialog;
        }

    }

}

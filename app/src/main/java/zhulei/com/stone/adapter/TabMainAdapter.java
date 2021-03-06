package zhulei.com.stone.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import zhulei.com.stone.R;
import zhulei.com.stone.activity.ImageActivity;
import zhulei.com.stone.entity.Comment;
import zhulei.com.stone.entity.Message;
import zhulei.com.stone.entity.User;
import zhulei.com.stone.util.ImageUtil;
import zhulei.com.stone.widget.BageTextView;

/**
 * Created by zhulei on 16/6/6.
 */
public class TabMainAdapter extends RecyclerView.Adapter<TabMainAdapter.TabViewHolder> {

    public static Object TAG = new Object();

    private Context mContext;
    private List<Message> mData;
    private Map<String, Integer> mCommentCount;

    private int mOldPosition = -1;
    private Listener mListener;

    public TabMainAdapter(Context context, ArrayList<Message> data){
        this.mContext =context;
        this.mData = data;
        mCommentCount = new HashMap<>();
    }

    public void setListener(Listener listener){
        this.mListener = listener;
    }

    public void onRefresh(){
        mOldPosition = -1;
    }

    @Override
    public TabMainAdapter.TabViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TabViewHolder tabVh = new TabViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_main_tab, parent, false));
        return tabVh;
    }

    @Override
    public void onBindViewHolder(final TabMainAdapter.TabViewHolder holder, final int position) {
        final Message message = mData.get(position);
        User user = message.getUser();
        String userHeader = user.getHeader();
        String userName = user.getUsername();
        String text = message.getText();
        ArrayList<String> images = ImageUtil.getImages(message.getImages());
        String create = message.getCreatedAt();
        if (!TextUtils.isEmpty(userHeader)){
            Picasso.with(mContext)
                    .load(userHeader)
                    .tag(TAG)
                    .config(Bitmap.Config.ALPHA_8)
                    .resize(mContext.getResources().getDimensionPixelSize(R.dimen.size_thumbnail_small),
                            mContext.getResources().getDimensionPixelSize(R.dimen.size_thumbnail_small))
                    .centerCrop()
                    .error(R.drawable.loading_fail)
                    .placeholder(R.drawable.ic_loading)
                    .into(holder.mUserHeader);
        }else {
            Picasso.with(mContext)
                    .load(R.drawable.user_header)
                    .tag(TAG)
                    .config(Bitmap.Config.ALPHA_8)
                    .resize(mContext.getResources().getDimensionPixelSize(R.dimen.size_thumbnail_small),
                            mContext.getResources().getDimensionPixelSize(R.dimen.size_thumbnail_small))
                    .centerCrop()
                    .into(holder.mUserHeader);
        }
        if (userName != null){
            holder.mUserName.setText(userName);
        }
        if (text != null){
            holder.mTvContent.setText(text);
        }
        if (create != null){
            holder.mCreate.setText(create);
        }
        if (images != null && !images.isEmpty()){
            holder.mImages.setVisibility(View.VISIBLE);
            if (images.size() <= 4){
                holder.mImages.setNumColumns(2);
            }else {
                holder.mImages.setNumColumns(3);
            }
            holder.mImages.setAdapter(new ImageAdater(images));
        }else {
            holder.mImages.setVisibility(View.GONE);
        }

        holder.mBageTextView.setVisibility(View.GONE);
        if (position > mOldPosition){
            final BmobQuery<Comment> query = new BmobQuery<>();
            query.addWhereEqualTo("message", message);
            query.setLimit(10);
            query.setSkip(0);
            query.findObjects(mContext, new FindListener<Comment>() {
                @Override
                public void onSuccess(List<Comment> list) {
                    if (mContext != null && holder.mBageTextView != null){
                        if (list != null){
                            mCommentCount.put(message.getObjectId(), list.size());
                            setCommentCount(message.getObjectId(), holder.mBageTextView);
                        }
                    }
                }

                @Override
                public void onError(int i, String s) {
                }
            });

            AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(mContext, R.anim.card_item);
            holder.mCardItem.setAnimation(set);
            set.start();
            mOldPosition = position;
        }else {
            setCommentCount(message.getObjectId(), holder.mBageTextView);
        }

        holder.mIvComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onCommentClicked(message);
                }
            }
        });

    }

    private void setCommentCount(String messageId, TextView commentCount){
        if (mCommentCount.get(messageId) != null){
            int count = mCommentCount.get(messageId);
            if (count == 0){
                commentCount.setVisibility(View.GONE);
            }else if (count < 10){
                commentCount.setVisibility(View.VISIBLE);
                commentCount.setText(count + "");
            }else {
                commentCount.setVisibility(View.VISIBLE);
                commentCount.setText("...");
            }
        }else {
            commentCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewDetachedFromWindow(TabViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.mCardItem.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class TabViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.card_item)
        CardView mCardItem;
        @BindView(R.id.user_header)
        ImageView mUserHeader;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.tv_content)
        TextView mTvContent;
        @BindView(R.id.gv_images)
        GridView mImages;
        @BindView(R.id.tv_create)
        TextView mCreate;
        @BindView(R.id.iv_comment)
        ImageView mIvComment;
        @BindView(R.id.bage)
        BageTextView mBageTextView;

        public TabViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ImageAdater extends BaseAdapter{

        private ArrayList<String> mData;

        public ImageAdater(ArrayList<String> data){
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null){
                itemView = LayoutInflater.from(mContext).inflate(R.layout.item_image_main, parent, false);
                ImageViewHolder holder = new ImageViewHolder(itemView);
                itemView.setTag(holder);
            }
            ImageViewHolder holder = (ImageViewHolder) itemView.getTag();
            holder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ImageActivity.class);
                    intent.putStringArrayListExtra(ImageActivity.ARG_IMAGES, mData);
                    intent.putExtra(ImageActivity.ARG_POSITION, position);
                    mContext.startActivity(intent);
                }
            });
            int with = mContext.getResources().getDimensionPixelSize(R.dimen.size_thumbnail);
            int height = with;
            ViewGroup.LayoutParams params = holder.mImage.getLayoutParams();
            params.height = mContext.getResources().getDisplayMetrics().widthPixels / 2;
            if (mData.size() >= 5){
                params.height = mContext.getResources().getDisplayMetrics().widthPixels / 3;
            }
            Picasso.with(mContext)
                    .load(mData.get(position))
                    .tag(TAG)
                    .resize(with, height)
                    .config(Bitmap.Config.ALPHA_8)
                    .centerCrop()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.loading_fail)
                    .into(holder.mImage);
            return itemView;
        }

        class ImageViewHolder{

            @BindView(R.id.image)
            ImageView mImage;

            public ImageViewHolder(View itemView){
                ButterKnife.bind(this, itemView);
            }

        }
    }

    public interface Listener{
        void onCommentClicked(Message message);
    }
}

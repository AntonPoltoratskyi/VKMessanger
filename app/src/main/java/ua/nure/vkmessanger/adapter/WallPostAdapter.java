package ua.nure.vkmessanger.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ua.nure.vkmessanger.R;
import ua.nure.vkmessanger.model.Link;
import ua.nure.vkmessanger.model.Photo;

/**
 * Адаптер для списка фотографий и ссылок ('link'), которые приклеплены к записи на стене.
 * Адаптер также обеспечивает работу с Header-ом - заголовком, который
 * отображает владельца стены, на которой была размещена запись.
 */
public class WallPostAdapter extends RecyclerView.Adapter<WallPostAdapter.WallPostItemViewHolder> {

    private static final int TYPE_HEADER_LAYOUT = 1;
    private static final int TYPE_PHOTO_LAYOUT = R.layout.attachment_item_photo;
    private static final int TYPE_LINK_LAYOUT = R.layout.attachment_item_link_for_wall_post;

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    /**
     * Заголовок, представляющий собой владельца стены, на которой была размещена запись.
     */
    private View mHeader;

    @Nullable
    private List<Photo> mPhotos;

    /**
     * Запись на стене также может содержать ссылку ('link').
     */
    @Nullable
    private Link mLink;

    /**
     * Обработчик кликов по ссылке.
     */
    private OnAttachmentsItemClickListener mOnAttachmentsItemClickListener;


    public WallPostAdapter(Context context, View header, @Nullable List<Photo> photos, @Nullable Link link) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mHeader = header;
        mPhotos = photos;
        mLink = link;
        mOnAttachmentsItemClickListener = new OnAttachmentsItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (isLink(position)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mLink.getURL()));
                    mContext.startActivity(intent);
                }
            }
        };
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    public boolean isLink(int position) {
        //Учитываю mHeader и mPhotos при проверке позиции.
        return mLink != null && position == mPhotos.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return TYPE_HEADER_LAYOUT;
        } else if (isLink(position)) {
            return TYPE_LINK_LAYOUT;
        }
        return TYPE_PHOTO_LAYOUT;
    }

    @Override
    public WallPostItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER_LAYOUT) {
            return new WallPostItemViewHolder(mContext, mHeader, viewType, mOnAttachmentsItemClickListener);
        }
        View view = mLayoutInflater.inflate(viewType, parent, false);
        return new WallPostItemViewHolder(mContext, view, viewType, mOnAttachmentsItemClickListener);
    }

    @Override
    public void onBindViewHolder(WallPostItemViewHolder holder, int position) {
        if (isHeader(position)) {
            //because header logic is in Activity.
            return;
        }
        if (!isLink(position) && mPhotos != null) {
            //position - 1, т.к. учитываю header.
            holder.bindPhoto(position - 1, mPhotos);
        } else if (isLink(position)) {
            holder.bindLink(mLink);
        }
    }

    @Override
    public int getItemCount() {
        //Учитываю mHeader и mLink.
        int hasLink = mLink == null ? 0 : 1;
        return mPhotos == null ?
                hasLink + 1 :
                mPhotos.size() + hasLink + 1;
    }


    static class WallPostItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Picasso mPicasso;

        private OnAttachmentsItemClickListener mAttachmentsItemClickListener;

        //----------Photo----------//

        private ImageView mPhotoImageView;

        //-----------Link---------//

        private ImageView mLinkImageView;

        private TextView mLinkTitleTV;

        private TextView mLinkDescriptionTV;


        public WallPostItemViewHolder(Context context, View itemView, int viewType, OnAttachmentsItemClickListener listener) {
            super(itemView);
            mPicasso = Picasso.with(context);
            mAttachmentsItemClickListener = listener;

            //Photo.
            if (viewType == TYPE_PHOTO_LAYOUT) {
                mPhotoImageView = (ImageView) itemView.findViewById(R.id.attachmentPhotoImageView);
            }

            //Link.
            if (viewType == TYPE_LINK_LAYOUT) {
                mLinkImageView = (ImageView) itemView.findViewById(R.id.linkImageView);
                mLinkTitleTV = (TextView) itemView.findViewById(R.id.linkTitleTV);
                mLinkDescriptionTV = (TextView) itemView.findViewById(R.id.linkDescriptionTV);
            }
            itemView.setOnClickListener(this);
        }

        public void bindPhoto(int position, List<Photo> photos) {
            String photoURL = photos.get(position).getNormalSizePhotoURL();
            mPicasso.load(photoURL).into(mPhotoImageView);
        }

        public void bindLink(final Link link) {
            Photo linkPhoto = link.getPhoto();
            if (linkPhoto != null) {
                mPicasso.load(linkPhoto.getMaxSizePhotoURL()).into(mLinkImageView);
            }
            mLinkTitleTV.setText(link.getTitle());
            mLinkDescriptionTV.setText(link.getDescription());
        }

        @Override
        public void onClick(View v) {
            if (mAttachmentsItemClickListener != null) {
                mAttachmentsItemClickListener.onItemClick(getLayoutPosition());
            }
        }
    }


    /**
     * Слушатель кликов по ссылке Link.
     */
    public interface OnAttachmentsItemClickListener {
        void onItemClick(int position);
    }

}
package com.selfcompany.yandexmusiclist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class JSONAdapter extends BaseAdapter implements Filterable{

    Context mContext;
    LayoutInflater mInflater;
    List<Artist> mJsonArray, copy;
    ViewHolder holder;

    public JSONAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mJsonArray = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mJsonArray.size();
    }

    @Override
    public Artist getItem(int position) {
        return mJsonArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateData(List<Artist> jsonArray) {
        //обновление данных адаптера
        mJsonArray = jsonArray;
        copy = jsonArray;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // проверка вью на существование
        if (convertView == null) {
            // заполнить вью схемой вью из списка
            convertView = mInflater.inflate(R.layout.row_music, parent, false);

            // новый "холдер" с определением внутренних вью
            holder = new ViewHolder();
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.img_thumbnail);
            holder.artistTextView = (TextView) convertView.findViewById(R.id.text_artist);
            holder.genresTextView = (TextView) convertView.findViewById(R.id.genres);

            // запоминаем
            convertView.setTag(holder);
        } else {
            // нет необходимости определять новый "холдер"... просто загрузим уже запомненный
            holder = (ViewHolder) convertView.getTag();
        }
        // заполняем внутренние вью конкретной инфо
        final Artist artist = getItem(position);

        //загрузка маленького аватара
        //сначала пытаемся вытащить из кеша
        Picasso.with(mContext).load(artist.getCover().getSmall()).networkPolicy(NetworkPolicy.OFFLINE).
                placeholder(R.drawable.music_android_r).into(holder.thumbnailImageView, new Callback() {
            //получилось - даем знать адаптеру об этом
            @Override
            public void onSuccess() {
                notifyDataSetChanged();
            }

            @Override
            public void onError() {
                //не получается... чтож... идем в интернет
                Picasso.with(mContext).load(artist.getCover().getSmall()).
                        placeholder(R.drawable.music_android_r).into(holder.thumbnailImageView, new Callback() {
                    //получилось - даем знать адаптеру об этом
                    @Override
                    public void onSuccess() {
                        notifyDataSetChanged();
                    }

                    //беда какая-то
                    @Override
                    public void onError() {
                        Log.v("Picasso", "Could not fetch image");
                        Toast.makeText(mContext, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //исполнитель
        if (artist.getName().isEmpty()) holder.artistTextView.setText("Без имени");
        else
        holder.artistTextView.setText(artist.getName());
        //жанры
        if (artist.getGenres().size() == 0) holder.genresTextView.setText("Жанры не указаны");
        else {
            StringBuilder sb = new StringBuilder();
            for (String genr : artist.getGenres()) {
                sb.append(genr + " ");
            }
            holder.genresTextView.setText(sb.toString().trim().replaceAll(" ", ", "));
        }

        return convertView;
    }

    //фильтр для поиска по исполнителям
    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            //обновление списка исполнителей в соответствии с текущим состоянием поиска
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mJsonArray = (List<Artist>) results.values;
                notifyDataSetChanged();
            }

            //сам фильтр динамического поиска, результат идет в publishResults(-||-)
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                mJsonArray = copy;
                FilterResults results = new FilterResults();
                ArrayList<Artist> FilteredArrayNames = new ArrayList<>();

                // алгоритм фильтра и создание списка совпадений

                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < mJsonArray.size(); i++) {
                    Artist artist = mJsonArray.get(i);
                    if (artist.getName().toLowerCase().contains(constraint.toString()))  {
                        FilteredArrayNames.add(artist);
                    }
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;
                Log.e("VALUES", results.values.toString());

                return results;
            }
        };

        return filter;
    }

    public static class ViewHolder {
        public ImageView thumbnailImageView;
        public TextView artistTextView;
        public TextView genresTextView;
    }
}

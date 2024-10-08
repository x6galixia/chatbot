package com.example.aimindfultalks;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {
    private List<String> resourceList;

    public ResourceAdapter(List<String> resourceList) {
        this.resourceList = resourceList;
    }

    @Override
    public ResourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.resource_item, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResourceViewHolder holder, int position) {
        holder.resourceTextView.setText(resourceList.get(position));
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    public static class ResourceViewHolder extends RecyclerView.ViewHolder {
        TextView resourceTextView;

        public ResourceViewHolder(View itemView) {
            super(itemView);
            resourceTextView = itemView.findViewById(R.id.resourceTextView);
        }
    }
}
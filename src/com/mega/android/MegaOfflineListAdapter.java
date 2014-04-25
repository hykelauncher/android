package com.mega.android;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import com.mega.sdk.MegaApiAndroid;
import com.mega.sdk.MegaError;
import com.mega.sdk.MegaNode;
import com.mega.sdk.NodeList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MegaOfflineListAdapter extends BaseAdapter implements OnClickListener {
	
	Context context;

	int positionClicked;

	ArrayList<String> paths = new ArrayList<String>();	
	
	ListView listFragment;
	ImageView emptyImageViewFragment;
	TextView emptyTextViewFragment;
	ActionBar aB;
	
	boolean multipleSelect;
	
	/*public static view holder class*/
    public class ViewHolderBrowserList {
    	CheckBox checkbox;
        ImageView imageView;
        TextView textViewFileName;
        TextView textViewFileSize;
        ImageButton imageButtonThreeDots;
        RelativeLayout itemLayout;
        ImageView arrowSelection;
        RelativeLayout optionsLayout;
//        ImageButton optionOpen;
        ImageView optionOpen;
        ImageView optionProperties;
        ImageView optionDelete;
        int currentPosition;
        long document;
    }
	
	public MegaOfflineListAdapter(Context _context, ArrayList<String> _paths, ListView listView, ImageView emptyImageView, TextView emptyTextView, ActionBar aB) {
		this.context = _context;
		this.paths = _paths;

		this.listFragment = listView;
		this.emptyImageViewFragment = emptyImageView;
		this.emptyTextViewFragment = emptyTextView;
		this.aB = aB;
		
		this.positionClicked = -1;
	}
	
	public void setPaths(ArrayList<String> paths){
		this.paths = paths;
		positionClicked = -1;	
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v;
	
		listFragment = (ListView) parent;
		
		final int _position = position;
		
		ViewHolderBrowserList holder = null;
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = ((Activity)context).getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_offline_list, parent, false);
			holder = new ViewHolderBrowserList();
			holder.itemLayout = (RelativeLayout) convertView.findViewById(R.id.offline_list_item_layout);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.offline_list_checkbox);
			holder.checkbox.setClickable(false);
			holder.imageView = (ImageView) convertView.findViewById(R.id.offline_list_thumbnail);
			holder.textViewFileName = (TextView) convertView.findViewById(R.id.offline_list_filename);
			holder.textViewFileName.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			holder.textViewFileName.getLayoutParams().width = Util.px2dp((225*scaleW), outMetrics);
			holder.textViewFileSize = (TextView) convertView.findViewById(R.id.offline_list_filesize);
			holder.imageButtonThreeDots = (ImageButton) convertView.findViewById(R.id.offline_list_three_dots);
			holder.optionsLayout = (RelativeLayout) convertView.findViewById(R.id.offline_list_options);
			holder.optionOpen = (ImageView) convertView.findViewById(R.id.offline_list_option_open);
			holder.optionOpen.getLayoutParams().width = Util.px2dp((35*scaleW), outMetrics);
			((TableRow.LayoutParams) holder.optionOpen.getLayoutParams()).setMargins(Util.px2dp((9*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
			holder.optionProperties = (ImageView) convertView.findViewById(R.id.offline_list_option_properties);
			holder.optionProperties.getLayoutParams().width = Util.px2dp((35*scaleW), outMetrics);
			((TableRow.LayoutParams) holder.optionProperties.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
			holder.optionDelete = (ImageView) convertView.findViewById(R.id.offline_list_option_delete);
			holder.optionDelete.getLayoutParams().width = Util.px2dp((35*scaleW), outMetrics);
			((TableRow.LayoutParams) holder.optionDelete.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
			holder.arrowSelection = (ImageView) convertView.findViewById(R.id.offline_list_arrow_selection);
			holder.arrowSelection.setVisibility(View.GONE);
			
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolderBrowserList) convertView.getTag();
		}
		
		if (!multipleSelect){
			holder.checkbox.setVisibility(View.GONE);
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
		}
		else{
			holder.checkbox.setVisibility(View.VISIBLE);
			holder.arrowSelection.setVisibility(View.GONE);
			holder.imageButtonThreeDots.setVisibility(View.GONE);
			
			SparseBooleanArray checkedItems = listFragment.getCheckedItemPositions();
			if (checkedItems.get(position, false) == true){
				holder.checkbox.setChecked(true);
			}
			else{
				holder.checkbox.setChecked(false);
			}
		}
		
		holder.currentPosition = position;
		
		String currentPath = (String) getItem(position);
		File currentFile = new File(currentPath);
		Bitmap thumb = null;
		
		holder.textViewFileName.setText(currentFile.getName());
		
		long fileSize = currentFile.length();
		holder.textViewFileSize.setText(Util.getSizeString(fileSize));
		holder.imageView.setImageResource(MimeType.typeForName(currentFile.getName()).getIconResourceId());
		
//		if (node.hasThumbnail()){
//			thumb = ThumbnailUtils.getThumbnailFromCache(node);
//			if (thumb != null){
//				holder.imageView.setImageBitmap(thumb);
//			}
//			else{
//				thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
//				if (thumb != null){
//					holder.imageView.setImageBitmap(thumb);
//				}
//				else{ 
//					try{
//						thumb = ThumbnailUtils.getThumbnailFromMegaList(node, context, holder, megaApi, this);
//					}
//					catch(Exception e){} //Too many AsyncTasks
//					
//					if (thumb != null){
//						holder.imageView.setImageBitmap(thumb);
//					}
//				}
//			}
//		}
//		else{
//			thumb = ThumbnailUtils.getThumbnailFromCache(node);
//			if (thumb != null){
//				holder.imageView.setImageBitmap(thumb);
//			}
//			else{
//				thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
//				if (thumb != null){
//					holder.imageView.setImageBitmap(thumb);
//				}
//				else{ 
//					try{
//						ThumbnailUtils.createThumbnailList(context, node, holder, megaApi, this);
//					}
//					catch(Exception e){} //Too many AsyncTasks
//				}
//			}			
//		}
				
		holder.imageButtonThreeDots.setTag(holder);
		holder.imageButtonThreeDots.setOnClickListener(this);
		
		if (positionClicked != -1){
			if (positionClicked == position){
				holder.arrowSelection.setVisibility(View.VISIBLE);
				LayoutParams params = holder.optionsLayout.getLayoutParams();
				params.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
				holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
				holder.imageButtonThreeDots.setImageResource(R.drawable.three_dots_background_grey);
				listFragment.smoothScrollToPosition(_position);
				
				holder.optionOpen.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
				((TableRow.LayoutParams) holder.optionOpen.getLayoutParams()).setMargins(Util.px2dp((9*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
				holder.optionProperties.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
				((TableRow.LayoutParams) holder.optionProperties.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
				holder.optionDelete.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
				((TableRow.LayoutParams) holder.optionDelete.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
				
//				if (type == ManagerActivity.CONTACT_FILE_ADAPTER){
//					holder.optionDownload.setVisibility(View.VISIBLE);
//					holder.optionProperties.setVisibility(View.VISIBLE);
//					holder.optionCopy.setVisibility(View.VISIBLE);
//					holder.optionMove.setVisibility(View.GONE);
//					holder.optionPublicLink.setVisibility(View.GONE);
//					holder.optionRename.setVisibility(View.GONE);
//					holder.optionDelete.setVisibility(View.GONE);
//					
//					holder.optionDownload.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionDownload.getLayoutParams()).setMargins(Util.px2dp((9*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionProperties.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionProperties.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionCopy.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionCopy.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//				}
//				else if (type == ManagerActivity.RUBBISH_BIN_ADAPTER){
//					holder.optionDownload.setVisibility(View.VISIBLE);
//					holder.optionProperties.setVisibility(View.VISIBLE);
//					holder.optionCopy.setVisibility(View.VISIBLE);
//					holder.optionMove.setVisibility(View.VISIBLE);
//					holder.optionPublicLink.setVisibility(View.GONE);
//					holder.optionRename.setVisibility(View.VISIBLE);
//					holder.optionDelete.setVisibility(View.VISIBLE);
//					
//					holder.optionDownload.getLayoutParams().width = Util.px2dp((44*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionDownload.getLayoutParams()).setMargins(Util.px2dp((9*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionProperties.getLayoutParams().width = Util.px2dp((44*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionProperties.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionCopy.getLayoutParams().width = Util.px2dp((44*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionCopy.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionMove.getLayoutParams().width = Util.px2dp((44*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionMove.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionRename.getLayoutParams().width = Util.px2dp((44*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionRename.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionDelete.getLayoutParams().width = Util.px2dp((44*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionDelete.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//				}
//				else if (type == ManagerActivity.SHARED_WITH_ME_ADAPTER){
//					holder.optionDownload.setVisibility(View.VISIBLE);
//					holder.optionProperties.setVisibility(View.VISIBLE);
//					holder.optionCopy.setVisibility(View.VISIBLE);
//					holder.optionMove.setVisibility(View.GONE);
//					holder.optionPublicLink.setVisibility(View.GONE);
//					holder.optionRename.setVisibility(View.VISIBLE);
//					holder.optionDelete.setVisibility(View.VISIBLE);
//					
//					holder.optionDownload.getLayoutParams().width = Util.px2dp((55*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionDownload.getLayoutParams()).setMargins(Util.px2dp((9*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionProperties.getLayoutParams().width = Util.px2dp((55*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionProperties.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionCopy.getLayoutParams().width = Util.px2dp((55*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionCopy.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionMove.getLayoutParams().width = Util.px2dp((55*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionMove.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionRename.getLayoutParams().width = Util.px2dp((55*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionRename.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//					holder.optionDelete.getLayoutParams().width = Util.px2dp((55*scaleW), outMetrics);
//					((TableRow.LayoutParams) holder.optionDelete.getLayoutParams()).setMargins(Util.px2dp((17*scaleH), outMetrics), Util.px2dp((4*scaleH), outMetrics), 0, 0);
//				}
			}
			else{
				holder.arrowSelection.setVisibility(View.GONE);
				LayoutParams params = holder.optionsLayout.getLayoutParams();
				params.height = 0;
				holder.itemLayout.setBackgroundColor(Color.WHITE);
				holder.imageButtonThreeDots.setImageResource(R.drawable.three_dots_background_white);
			}
		}
		else{
			holder.arrowSelection.setVisibility(View.GONE);
			LayoutParams params = holder.optionsLayout.getLayoutParams();
			params.height = 0;
			holder.itemLayout.setBackgroundColor(Color.WHITE);
			holder.imageButtonThreeDots.setImageResource(R.drawable.three_dots_background_white);
		}
		
//		holder.optionOpen.setTag(holder);
//		holder.optionOpen.setOnClickListener(this);
		
		holder.optionOpen.setTag(holder);
		holder.optionOpen.setOnClickListener(this);
		
		holder.optionProperties.setTag(holder);
		holder.optionProperties.setOnClickListener(this);
		
		holder.optionDelete.setTag(holder);
		holder.optionDelete.setOnClickListener(this);
		
		return convertView;
	}

	@Override
	public boolean isEnabled(int position) {
		return super.isEnabled(position);
	}

	@Override
    public int getCount() {
        return paths.size();
    }
 
    @Override
    public Object getItem(int position) {
        return paths.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }    
    
    public int getPositionClicked (){
    	return positionClicked;
    }
    
    public void setPositionClicked(int p){
    	positionClicked = p;
    }

	@Override
	public void onClick(View v) {
		ViewHolderBrowserList holder = (ViewHolderBrowserList) v.getTag();
		int currentPosition = holder.currentPosition;
		String currentPath = (String) getItem(currentPosition);
		File currentFile = new File(currentPath);
		
		switch (v.getId()){
			case R.id.offline_list_option_open:{
				positionClicked = -1;
				notifyDataSetChanged();
//				ArrayList<Long> handleList = new ArrayList<Long>();
//				handleList.add(n.getHandle());
//				if (type == ManagerActivity.CONTACT_FILE_ADAPTER){
//					((ContactFileListActivity)context).onFileClick(handleList);
//				}
//				else{
//					((ManagerActivity) context).onFileClick(handleList);
//				}
				break;
			}
			case R.id.offline_list_option_properties:{
//				Intent i = new Intent(context, FilePropertiesActivity.class);
//				i.putExtra("handle", n.getHandle());
//			
//				if (n.isFolder()){
//					i.putExtra("imageId", R.drawable.mime_folder);
//				}
//				else{
//					i.putExtra("imageId", MimeType.typeForName(n.getName()).getIconResourceId());	
//				}				
//				i.putExtra("name", n.getName());
//				context.startActivity(i);							
				positionClicked = -1;
				notifyDataSetChanged();
				break;
			}
			case R.id.offline_list_option_delete:{
				setPositionClicked(-1);
				notifyDataSetChanged();
//				ArrayList<Long> handleList = new ArrayList<Long>();
//				handleList.add(n.getHandle());				
//				if (type != ManagerActivity.CONTACT_FILE_ADAPTER){
//					((ManagerActivity) context).moveToTrash(handleList);
//				}
				break;
			}
			case R.id.offline_list_three_dots:{
				if (positionClicked == -1){
					positionClicked = currentPosition;
					notifyDataSetChanged();
				}
				else{
					if (positionClicked == currentPosition){
						positionClicked = -1;
						notifyDataSetChanged();
					}
					else{
						positionClicked = currentPosition;
						notifyDataSetChanged();
					}
				}
				break;
			}
		}		
	}
	
	/*
	 * Get path at specified position
	 */
	public String getPathAt(int position) {
		try {
			if(paths != null){
				return paths.get(position);
			}
		} catch (IndexOutOfBoundsException e) {}
		return null;
	}
	
	public boolean isMultipleSelect() {
		return multipleSelect;
	}

	public void setMultipleSelect(boolean multipleSelect) {
		if(this.multipleSelect != multipleSelect){
			this.multipleSelect = multipleSelect;
			notifyDataSetChanged();
		}
	}
	
	private static void log(String log) {
		Util.log("MegaBrowserListAdapter", log);
	}
}

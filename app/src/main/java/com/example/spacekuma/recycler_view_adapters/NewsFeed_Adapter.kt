package com.example.spacekuma.recycler_view_adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.spacekuma.GlideApp
import com.example.spacekuma.R

import com.example.spacekuma.data.NewsFeed_Model
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.get
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.spacekuma.Test.ViewPagerAdapter
import com.example.spacekuma.activities.Comment_Activity
import com.example.spacekuma.activities.Edit_FeedActivity
import com.example.spacekuma.activities.WriteFeedActivity
import com.example.spacekuma.data.Check_Model
import com.example.spacekuma.data.FeedDetail_Model
import com.example.spacekuma.retrofit.ApiClient
import com.example.spacekuma.util.TimeString
import kotlinx.android.synthetic.main.news_feed_default_item.view.*
import me.relex.circleindicator.CircleIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NewsFeed_Adapter(val Num : Int, val ID : String, val Pic : String, val context: Context, val newsfeedList: ArrayList<NewsFeed_Model>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_ONE = 1
        const val TYPE_LOADING = 777
    }


    inner class Default_ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) , View.OnCreateContextMenuListener{
        override fun onCreateContextMenu(menu: ContextMenu?,v: View?,menuInfo: ContextMenu.ContextMenuInfo?) {
            val Edit = menu!!.add(Menu.NONE,1001,1,"수정")
            val Delete = menu.add(Menu.NONE,1002,2,"삭제")

            Edit.setOnMenuItemClickListener(onMenuItemClickListener)
            Delete.setOnMenuItemClickListener(onMenuItemClickListener)
        }

        val onMenuItemClickListener : MenuItem.OnMenuItemClickListener = MenuItem.OnMenuItemClickListener {
            when (it.itemId) {
                1001 -> {
                    (context as Activity).startActivityForResult(Intent(context, Edit_FeedActivity::class.java)
                        .putExtra("Feed_Num",newsfeedList[adapterPosition].Feed_Num)
                        .putExtra("Feed_Text",newsfeedList[adapterPosition].Feed_Text)
                        .putExtra("Position",adapterPosition),666)
                    return@OnMenuItemClickListener true
                }
                1002 -> {
                    ApiClient.getClient.Delete_Feed_Item(newsfeedList[adapterPosition].Feed_Num).enqueue(object : Callback<Check_Model>{
                        override fun onFailure(call: Call<Check_Model>, t: Throwable) {
                            Toast.makeText(context,"다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<Check_Model>,response: Response<Check_Model>) {
                            if (response.isSuccessful && response.body()!!.Success) {
                                newsfeedList.removeAt(adapterPosition)
                                notifyItemRemoved(adapterPosition)
                            } else {
                                Toast.makeText(context,"삭제실패.",Toast.LENGTH_SHORT).show()
                            }
                        }

                    })
                    return@OnMenuItemClickListener true
                }
            }
            return@OnMenuItemClickListener false
        }

        val ViewPager_FeedMedia = itemView.findViewById<ViewPager>(R.id.ViewPager_FeedMedia)

        val Con_Like = itemView.findViewById<ConstraintLayout>(R.id.Con_Like)
        val Con_Comment = itemView.findViewById<ConstraintLayout>(R.id.Con_Comment)

        val ImageView_User_Pic = itemView.findViewById<ImageView>(R.id.ImageView_Writer_Pic)
        val ImageView_Before_Like = itemView.findViewById<ImageView>(R.id.ImageView_Before_Like)
        val ImageView_After_Like = itemView.findViewById<ImageView>(R.id.ImageView_After_Like)
        val ImageView_Comment = itemView.findViewById<ImageView>(R.id.ImageView_Comment)

        val TextView_User_Name = itemView.findViewById<TextView>(R.id.TextView_User_Name)
        val TextView_Uploaded_Date = itemView.findViewById<TextView>(R.id.TextView_Uploaded_Date)
        val TextView_Like_Count = itemView.findViewById<TextView>(R.id.TextView_Like_Count)
        val TextView_Comment_Count = itemView.findViewById<TextView>(R.id.TextView_Comment_Count)
        val TextView_Feed_Content = itemView.findViewById<TextView>(R.id.TextView_Feed_Content)
        val TextView_Limit_Comment = itemView.findViewById<TextView>(R.id.TextView_Limit_Comment)

        val Btn_Edit = itemView.findViewById<ImageButton>(R.id.Btn_Edit)

        val indicator = itemView.findViewById<CircleIndicator>(R.id.indicator)

        fun bind(newsfeedModel: NewsFeed_Model, context: Context) {
            Log.e("Default_ViewHolder", "Feed_Num : " + newsfeedModel.Feed_Num.toString())
            Log.e("Default_ViewHolder", "Liked : " + newsfeedModel.Liked)
            Log.e("Default_ViewHolder", "Limit_Comment : " + newsfeedModel.Limit_Comment.toString())
            TextView_User_Name.text = newsfeedModel.Writer_Name
            TextView_Feed_Content.text = newsfeedModel.Feed_Text
            TextView_Uploaded_Date.text = TimeString.formatTimeString(newsfeedModel.Uploaded_Date)
            TextView_Like_Count.text = countSetText(newsfeedModel.Like_Count)
            TextView_Comment_Count.text = countSetText(newsfeedModel.Comment_Count)

            if (newsfeedModel.Writer_Num == Num) {
                Btn_Edit.visibility = View.VISIBLE
            } else {
                Btn_Edit.visibility = View.GONE
            }

            Btn_Edit.setOnCreateContextMenuListener(this)

            Was_Liked(ImageView_Before_Like,ImageView_After_Like,newsfeedModel.Liked,Num)

            Feed_Item_Is_Limit_Comment(TextView_Limit_Comment,newsfeedModel.Limit_Comment)

            val viewPagerAdapter : ViewPagerAdapter =  ViewPagerAdapter(newsfeedModel.Feed_Media_Uri!!,context)
            ViewPager_FeedMedia.adapter = viewPagerAdapter
            indicator.setViewPager(ViewPager_FeedMedia)

            ImageView_Before_Like.setOnClickListener {
                Feed_Like(newsfeedModel.Feed_Num,Num,ImageView_Before_Like,ImageView_After_Like,TextView_Like_Count,newsfeedModel)
            }

            ImageView_After_Like.setOnClickListener {
                Feed_UnLike(newsfeedModel.Feed_Num,Num,ImageView_Before_Like,ImageView_After_Like,TextView_Like_Count,newsfeedModel)
            }

            ImageView_Comment.setOnClickListener {
                (context as Activity).startActivityForResult(Intent(context, Comment_Activity::class.java)
                    .putExtra("Num",Num)
                    .putExtra("Feed_Num",newsfeedModel.Feed_Num)
                    .putExtra("Writer_Pic",newsfeedModel.Writer_Pic)
                    .putExtra("Writer_Name",newsfeedModel.Writer_Name)
                    .putExtra("Writer_Num",newsfeedModel.Writer_Num),888)
            }

        }
    }

    inner class Loading_ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(newsfeedModel: NewsFeed_Model, context: Context) {

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ONE -> {
                Default_ViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.news_feed_default_item, parent, false)
                )
            }

            TYPE_LOADING -> {
                Loading_ViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.news_feed_loading_item, parent, false)
                )
            }

            else -> throw IllegalArgumentException("Invalid view type")

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Default_ViewHolder -> {
                holder.itemView.run {
                    holder.bind(newsfeedList[position],context)
                }
            }

            is Loading_ViewHolder -> {
                holder.itemView.run {
                    holder.bind(newsfeedList[position],context)
                }
            }

        }

    }

    override fun getItemCount(): Int {
        return newsfeedList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (newsfeedList[position].View_Type) {
            1 -> TYPE_ONE
            777 -> TYPE_LOADING
            else -> TYPE_ONE
        }
    }

    fun addLoadingItem() {
        newsfeedList.add(NewsFeed_Model(0, TYPE_LOADING,0,0,0,0,true,true,"","","",null,"","",""))
        notifyItemInserted(newsfeedList.size - 1)
    }

    fun removeLoadingItem() {
        newsfeedList.removeAt(newsfeedList.size-1)
    }

    fun clear() {
        newsfeedList.clear()
    }

    fun addNextItem (NextItem: ArrayList<NewsFeed_Model>) {
        newsfeedList.addAll(NextItem)
    }

    fun countSetText (count : Int) : String {
        if (count == 0) {
            return ""
        } else {
            return count.toString()
        }
    }

    fun Was_Liked(Before_Like : ImageView, After_Like : ImageView, Liked : Int, Num : Int) {
        if (Liked == Num) {
            After_Like.visibility = View.VISIBLE
            Before_Like.visibility = View.GONE
        } else {
            After_Like.visibility = View.GONE
            Before_Like.visibility = View.VISIBLE
        }
    }

    // 댓글 제한 기능인데 아직 추가된 기능이 아닙니다.
    fun Feed_Item_Is_Limit_Comment(TextView_Limit_Comment : TextView ,Limit_Comment : Boolean) {
        if (Limit_Comment) {
            TextView_Limit_Comment.visibility = View.VISIBLE
        } else {
            TextView_Limit_Comment.visibility = View.GONE
        }
    }

    fun Feed_Like (Feed_Num : Int, Who_Liked : Int, Btn_Like : ImageView, Btn_UnLike : ImageView, Like_CountView : TextView,newsfeedModel: NewsFeed_Model) {
        ApiClient.getClient.Update_Feed_Like(Feed_Num,Who_Liked).enqueue(object : Callback<Check_Model> {
            override fun onFailure(call: Call<Check_Model>, t: Throwable) {

            }

            override fun onResponse(call: Call<Check_Model>, response: Response<Check_Model>) {
                if (response.isSuccessful) {
                    if (response.body()!!.Success) {
                        Btn_Like.visibility = View.GONE
                        Btn_UnLike.visibility = View.VISIBLE
                        newsfeedModel.Like_Count = newsfeedModel.Like_Count + 1
                        newsfeedModel.Liked = Num
                        Like_CountView.text = countSetText(newsfeedModel.Like_Count)
                    } else {

                    }
                } else {

                }
            }

        })
    }

    fun Feed_UnLike (Feed_Num : Int, Who_Liked : Int, Btn_Like : ImageView, Btn_UnLike : ImageView, Like_CountView : TextView,newsfeedModel: NewsFeed_Model) {
        ApiClient.getClient.Update_Feed_UnLike(Feed_Num,Who_Liked).enqueue(object : Callback<Check_Model> {
            override fun onFailure(call: Call<Check_Model>, t: Throwable) {

            }

            override fun onResponse(call: Call<Check_Model>, response: Response<Check_Model>) {
                if (response.isSuccessful) {
                    if (response.body()!!.Success) {
                        Btn_UnLike.visibility = View.GONE
                        Btn_Like.visibility = View.VISIBLE
                        newsfeedModel.Liked = 0
                        newsfeedModel.Like_Count = newsfeedModel.Like_Count - 1
                        Like_CountView.text = countSetText(newsfeedModel.Like_Count)
                    } else {

                    }
                } else {

                }
            }

        })
    }

}
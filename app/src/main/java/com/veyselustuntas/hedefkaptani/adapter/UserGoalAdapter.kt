package com.veyselustuntas.hedefkaptani.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.veyselustuntas.hedefkaptani.utils.Target
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.view.fragment.UserGoalDetailDirections
import com.veyselustuntas.hedefkaptani.view.fragment.UserGoalsDirections

class UserGoalAdapter (val userTarget:ArrayList<Target>) : RecyclerView.Adapter<UserGoalAdapter.UserGoalVH>() {

    class UserGoalVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserGoalVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_goal_detail_recycler_row,parent,false)
        return UserGoalVH(itemView)
    }

    override fun onBindViewHolder(holder: UserGoalVH, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.goalNameEditText).text = userTarget.get(position).targetName
        holder.itemView.findViewById<TextView>(R.id.goalDateEditText).text = userTarget.get(position).targetDate
        holder.itemView.findViewById<TextView>(R.id.goalTimeEditText).text = userTarget.get(position).targetTime

        holder.itemView.setOnClickListener {
            val currentDocId = userTarget.get(position).targetDocId
            val action = UserGoalsDirections.actionNavMyGoalsToUserGoalDetail(currentDocId)
            Navigation.findNavController(it).navigate(action)
        }


    }

    override fun getItemCount(): Int {
        return userTarget.size
    }
}
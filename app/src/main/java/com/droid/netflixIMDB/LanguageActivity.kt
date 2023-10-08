package com.droid.netflixIMDB

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blongho.country_data.World


class LanguageActivity : AppCompatActivity() {

  private lateinit var languageList: List<LanguageOption>
  private lateinit var rvLanguages: RecyclerView
  private lateinit var ivLanguageSelected: ImageView
  private lateinit var tvLanguageHeader: TextView
  private lateinit var languageAdapter: LanguageAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_language)

    initUi()
    initLanguages()
  }

  private fun initUi() {
    rvLanguages = findViewById<RecyclerView>(R.id.rvLanguages)
    ivLanguageSelected = findViewById<ImageView>(R.id.ivLanguageSelectionDone)
    tvLanguageHeader = findViewById<TextView>(R.id.tvLanguageHeader)
  }

  private fun initLanguages() {
    languageAdapter = LanguageAdapter { languageSelected ->
      toast("Clicked ${languageSelected.language.name}")
      val updatedList = buildLanguageList() as ArrayList<LanguageOption>

      val languageToUpdate = updatedList.find { it == languageSelected }
      val languageToUpdateIndex = updatedList.indexOf(languageToUpdate)

      languageToUpdate?.let {
        val updatedLanguage = it.copy(isSelected = true)
        updatedList.set(languageToUpdateIndex, updatedLanguage)
      }

      languageAdapter.submitList(updatedList)

      ContextUtils.setAppLocale(this, languageSelected.language.languageCode)
      tvLanguageHeader.text = this.resources.getString(R.string.language_selected_hint)
    }

    rvLanguages.apply {
      layoutManager = LinearLayoutManager(this@LanguageActivity)
      adapter = languageAdapter
    }

    languageList = buildLanguageList(true)
    languageAdapter.submitList(languageList)
  }

  inner class LanguageAdapter(private val callback: (languageOption: LanguageOption) -> Unit) :
    ListAdapter<LanguageOption, LanguageAdapter.ViewHolder>(LanguageOptionDiff()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      private var tvLanguageName: TextView
      private var rootLanguage: LinearLayout
      private var ivFlag: ImageView

      init {
        tvLanguageName = view.findViewById(R.id.tvLanguageName)
        ivFlag = view.findViewById(R.id.ivFlag)
        rootLanguage = view.findViewById(R.id.rootLanguage)
      }

      fun bind(languageOption: LanguageOption) {
        tvLanguageName.text = languageOption.language.name
        ivFlag.setImageResource(World.getFlagOf(languageOption.language.languageCode))

        if (languageOption.isSelected) {
          rootLanguage.isSelected = true
          tvLanguageName.isSelected = true
        } else {
          tvLanguageName.isSelected = false
          rootLanguage.isSelected = false
        }

        rootLanguage.setOnClickListener {
          if (!languageOption.isSelected) {
            callback.invoke(languageOption)
          }
        }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(getItem(position))
    }
  }

  class LanguageOptionDiff : DiffUtil.ItemCallback<LanguageOption>() {
    override fun areItemsTheSame(oldItem: LanguageOption, newItem: LanguageOption): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LanguageOption, newItem: LanguageOption): Boolean {
      return oldItem == newItem
    }
  }

  data class LanguageOption(val id: Int, val language: Language, var isSelected: Boolean = false)

  private fun buildLanguageList(isFirstFetch: Boolean = false): List<LanguageOption> {
    val languageOption = arrayListOf<LanguageOption>()
    Language.values().forEachIndexed { index, language ->
      if (index == 0 && isFirstFetch) {
        val option = LanguageOption(index, language).copy(isSelected = true)
        languageOption.add(option)
      } else {
        languageOption.add(LanguageOption(index, language))
      }
    }
    return languageOption
  }
}

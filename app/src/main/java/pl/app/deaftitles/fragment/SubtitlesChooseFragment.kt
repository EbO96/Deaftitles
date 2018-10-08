package pl.app.deaftitles.fragment


import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_subtitles_choose.*
import kotlinx.android.synthetic.main.subtitle_item.view.*
import pl.app.deaftitles.R
import pl.app.deaftitles.adapter.MySimpleAdapter
import pl.app.deaftitles.adapter.adapter
import pl.app.deaftitles.model.Subtitles
import pl.app.deaftitles.utils.timeToString
import pl.app.deaftitles.view.ActivityInteraction
import pl.app.deaftitles.view.DeaftitlesActivity

class SubtitlesChooseFragment : Fragment() {

    private var activityInteraction: ActivityInteraction<DeaftitlesActivity>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subtitles_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        //Get subtitles from local database
        activityInteraction?.getSubtitles()?.observe(this, Observer { cache ->

            //Hide loading view
            loadSubtitlesProgressBar.visibility = View.GONE

            if (cache != null) {
                if (subtitlesRecyclerView?.adapter == null) {
                    //Load into recycler view
                    subtitlesRecyclerView?.adapter(
                            item = R.layout.subtitle_item,
                            items = cache,
                            decoration = true,
                            viewHolder = { subtitles, view ->
                                view.apply {
                                    subtitleNameTextView.text = subtitles.name
                                    subtitleDateTextView.text = subtitles.timestampString
                                    subtitleLengthTextView.text = subtitles.movieLength.timeToString()

                                    val fileSize = "${String.format("%.2f", subtitles.fileSize)} kB"
                                    subtitleSizeTextView.text = fileSize

                                    deleteSubtitleButton?.setOnClickListener {
                                        activityInteraction?.deleteSubtitles(subtitles.name)
                                        cache.remove(subtitles)
                                        subtitlesRecyclerView?.adapter?.notifyDataSetChanged()
                                    }
                                }
                            },
                            click = { subtitles ->
                                activityInteraction?.onCacheSubtitlesSelected(subtitles)
                            })
                } else {
                    (subtitlesRecyclerView?.adapter as? MySimpleAdapter<Subtitles>)?.items = cache
                }
            }
        })

        backButton.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        addSubtitleButton.setOnClickListener {
            activityInteraction?.addNewSubtitles(loadSubtitlesProgressBar)
        }
    }

    fun showLoading() {
        loadSubtitlesProgressBar.visibility = View.VISIBLE
    }

    fun hideLoading() {
        loadSubtitlesProgressBar.visibility = View.INVISIBLE
    }

    @SuppressWarnings("unchecked")
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activityInteraction = context as? ActivityInteraction<DeaftitlesActivity>
    }

    override fun onDestroy() {
        activityInteraction?.onSubtitleChooseDestroyed()
        super.onDestroy()
    }

}

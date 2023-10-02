package com.example.pooptube.videodetail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import com.example.pooptube.R
import com.example.pooptube.databinding.FragmentVideoDetailBinding
import com.example.pooptube.home.HomeVideoModel
import com.example.pooptube.main.MainActivity
import com.example.pooptube.myvideos.VideosModelList
import java.util.Date

class VideoDetailFragment : Fragment() {

    private var _binding: FragmentVideoDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).showToolbar(false)
        (requireActivity() as MainActivity).showTabLayout(false)

        val bundle = arguments
        if (bundle != null) {
            val fragmentType = bundle.getInt("fragment", -1)

            when (fragmentType) {
                0 -> {
                    // Home 프래그먼트에서 클릭한 경우
                    val videoData =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            bundle.getParcelable("videoData", HomeVideoModel::class.java)
                        } else {
                            bundle.getParcelable("videoData")
                        }
                    if (videoData != null) {
                        val videoModel = VideoDetailModel(
                            thumbnail = videoData.imgThumbnail,
                            title = videoData.title,
                            channelProfile = videoData.imgThumbnail,
                            channelId = videoData.author,
                            description = videoData.description, // HomeVideoModel에는 description이 없어서 잠시 비워둠...
                            dateTime = videoData.dateTime,
                            viewCount = videoData.count,
                            isFavorite = false
                        )
                        bind(videoModel)
                    }
                }

                1 -> {
                    // 다른 프래그먼트에서 클릭한 경우
                    val videoData =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            bundle.getParcelable("videoData", VideosModelList::class.java)
                        } else {
                            bundle.getParcelable("videoData")
                        }
                    val position = bundle.getInt("position", -1)

                    if (videoData != null && position >= 0 && position < videoData.items.size) {
                        val clickedItem = videoData.items[position]
                        val videoModel = VideoDetailModel(
                            thumbnail = clickedItem.snippet.thumbnails.medium.url,
                            title = clickedItem.snippet.title,
                            channelProfile = clickedItem.snippet.thumbnails.medium.url,   // 지금 쓰는 엔드포인트에는 없어서 thumbnail로 대체
                            channelId = clickedItem.snippet.channelTitle,
                            description = clickedItem.snippet.description,
                            dateTime = clickedItem.snippet.publishedAt,
                            viewCount = clickedItem.statistics?.viewCount ?: "0",
//                    videoId = clickedItem.videoId,
                            isFavorite = false
                        )
                        bind(videoModel)
                    }
                }
            }
        }

        binding.videodetailBtnDown.setOnClickListener {
            closeFragment()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as MainActivity).showToolbar(true)
        (requireActivity() as MainActivity).showTabLayout(true)
        _binding = null
    }

    private fun bind(item: VideoDetailModel) = with(binding) {
        videodetailThumbnail.load(item.thumbnail)
        videodetailTitle.text = item.title
        videodetailChannelProfile.load(item.thumbnail)
        videodetailChannelName.text = item.channelId
        videodetailDescription.text = item.description
        videodetailUpdatedDate.text = formatDate(item.dateTime)
        videodetailViewcount.text = formatViewCount(item.viewCount.toInt())

        favoriteVideo(item)

//        shareVideo(item.videoId)
        shareVideo(item.thumbnail)
    }

    private fun favoriteVideo(viewModel: VideoDetailModel) = with(binding) {
        val btnLike = videodetailLikeContainer
        val likeIcon = videodetailLikeIcon
        var isFavorite = viewModel.isFavorite
        likeIcon.setImageResource(if (isFavorite) R.drawable.ic_like_filled else R.drawable.ic_like_empty)

        btnLike.setOnClickListener {
            isFavorite = !isFavorite
            val newIconResId = if (isFavorite) {
                showToast("좋아요 리스트에 추가되었습니다")
                R.drawable.ic_like_filled
            } else {
                showToast("좋아요 리스트에서 삭제되었습니다")
                R.drawable.ic_like_empty
            }
            likeIcon.setImageResource(newIconResId)
        }
    }

    private fun closeFragment() {
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun shareVideo(videoUrl: String) {
        val btnShare = binding.videodetailShareContainer
        btnShare.setOnClickListener {
//            val videoUrl = "https://www.youtube.com/watch?v=$videoId"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, videoUrl)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, "공유하기"))
        }
    }

    private fun formatDate(dateTime: Date): String {
        val currentTime = Date()
        val timeDifferenceMillis = currentTime.time - dateTime.time

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val monthsInMilli = daysInMilli * 30
        val yearsInMilli = daysInMilli * 365

        val years = timeDifferenceMillis / yearsInMilli
        val months = timeDifferenceMillis / monthsInMilli
        val days = timeDifferenceMillis / daysInMilli
        val hours = timeDifferenceMillis / hoursInMilli
        val minutes = timeDifferenceMillis / minutesInMilli
        val seconds = timeDifferenceMillis / secondsInMilli

        return when {
            years >= 1 -> "${years}년 전"
            months >= 1 -> "${months}개월 전"
            days >= 1 -> "${days}일 전"
            hours >= 1 -> "${hours}시간 전"
            minutes >= 1 -> "${minutes}분 전"
            else -> "${seconds}초 전"
        }
    }

    private fun formatViewCount(viewCount: Int): String {
        val formattedViewCount = when {
            viewCount == 0 -> "없음"
            viewCount >= 100000000 -> "${viewCount / 100000000}.${viewCount % 100000000 / 10000000}억"
            viewCount >= 10000 -> "${viewCount / 10000}.${viewCount % 10000 / 1000}만"
            viewCount >= 1000 -> "${viewCount / 1000}.${viewCount % 1000 / 100}천"
            else -> viewCount.toString()
        }
        return "조회수 $formattedViewCount"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
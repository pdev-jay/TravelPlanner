package com.jccgs.travelplanner_v2.ckim

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityInfoCkimBinding

class InfoActivity_CKim : AppCompatActivity() {

    lateinit var binding: ActivityInfoCkimBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoCkimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //공항버스 예매 및 노선 확인
        binding.ivAirportBus.setOnClickListener {
            moveToLink("https://www.airport.kr/ap/ko/tpt/busRouteList.do")
        }

        //예약한 항공편 체크인 카운터 위치 확인
        binding.ivCheckinLocation.setOnClickListener {
            moveToLink("https://www.airport.kr/ap/ko/dep/depPasSchList.do")
        }

        //시간대별 인천공항 혼잡도
        binding.ivAirportCongestion.setOnClickListener {
            moveToLink("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=%EC%9D%B8%EC%B2%9C%EA%B3%B5%ED%95%AD+%ED%98%BC%EC%9E%A1%EB%8F%84&oquery=%ED%99%98%EC%A0%84&tqi=hrylelprvxsssPlL7wlssssssx0-384702")
        }

        //수화물 제한 규정
        binding.ivBaggageRules.setOnClickListener {
            moveToLink("https://www.airport.kr/ap_lp/ko/dep/process/resart/resart.do")
        }

        //통신사별 로밍서비스
        binding.ivRoamingService.setOnClickListener {
            moveToLink("https://airport.kr/ap_lp/ko/qck/online/roamdep/roamdep.do")
        }

        //실시간 환율
        binding.ivExchange.setOnClickListener {
            moveToLink("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=%EC%8B%A4%EC%8B%9C%EA%B0%84+%ED%99%98%EC%9C%A8&oquery=%EC%84%B8%EA%B3%84+%EB%82%A0%EC%94%A8&tqi=hryDalp0Yidssug46zsssssss9h-063955")
        }


        //세계 시간
        binding.ivGlobalTime.setOnClickListener {
            moveToLink("https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=%EC%84%B8%EA%B3%84+%EC%8B%9C%EA%B0%84&oquery=%EC%84%B8%EA%B3%84+%EC%8B%9C%EC%B0%A8&tqi=hryqcwprvxZssA9%2BbWlssssssDZ-286748")
        }

        //세계 날씨
        binding.ivGlobalWeather.setOnClickListener {
            moveToLink("https://www.weather.go.kr/w/theme/world-weather.do")
        }

    }

    fun moveToLink(uri: String){
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        var uri = Uri.parse("${uri}")
        intent.data = uri
        startActivity(intent)
    }


}
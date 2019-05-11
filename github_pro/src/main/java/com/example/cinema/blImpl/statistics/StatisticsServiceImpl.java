package com.example.cinema.blImpl.statistics;

import com.example.cinema.bl.statistics.StatisticsService;
import com.example.cinema.data.statistics.StatisticsMapper;
import com.example.cinema.po.AudiencePrice;
import com.example.cinema.po.Hall;
import com.example.cinema.po.MovieScheduleTime;
import com.example.cinema.po.MovieTotalBoxOffice;
import com.example.cinema.po.ScheduleAudience;
import com.example.cinema.vo.AudiencePriceVO;
import com.example.cinema.vo.MoviePlacingRateVO;
import com.example.cinema.vo.MovieScheduleTimeVO;
import com.example.cinema.vo.MovieTotalBoxOfficeVO;
import com.example.cinema.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author fjj
 * @date 2019/4/16 1:34 PM
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;
    @Override
    public ResponseVO getScheduleRateByDate(Date date) {
        try{
            Date requireDate = date;
            if(requireDate == null){
                requireDate = new Date();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            requireDate = simpleDateFormat.parse(simpleDateFormat.format(requireDate));

            Date nextDate = getNumDayAfterDate(requireDate, 1);
            return ResponseVO.buildSuccess(movieScheduleTimeList2MovieScheduleTimeVOList(statisticsMapper.selectMovieScheduleTimes(requireDate, nextDate)));

        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getTotalBoxOffice() {
        try {
            return ResponseVO.buildSuccess(movieTotalBoxOfficeList2MovieTotalBoxOfficeVOList(statisticsMapper.selectMovieTotalBoxOffice()));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }
	
	
    @Override
    public ResponseVO getAudiencePriceSevenDays() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            Date startDate = getNumDayAfterDate(today, -6);
            List<AudiencePriceVO> audiencePriceVOList = new ArrayList<>();
            for(int i = 0; i < 7; i++){
                AudiencePriceVO audiencePriceVO = new AudiencePriceVO();
                Date date = getNumDayAfterDate(startDate, i);
                audiencePriceVO.setDate(date);
                List<AudiencePrice> audiencePriceList = statisticsMapper.selectAudiencePrice(date, getNumDayAfterDate(date, 1));
                double totalPrice = audiencePriceList.stream().mapToDouble(item -> item.getTotalPrice()).sum();
                audiencePriceVO.setPrice(Double.parseDouble(String.format("%.2f", audiencePriceList.size() == 0 ? 0 : totalPrice / audiencePriceList.size())));
                audiencePriceVOList.add(audiencePriceVO);
            }
            return ResponseVO.buildSuccess(audiencePriceVOList);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }
	
	private ResponseVO branch(Date date){
        //要求见接口说明
        try{
            //统计座位数
            List<Hall> statisticsHall = statisticsMapper.selectAllHall();
            int totalSeat = 0;
            for (Hall h: statisticsHall
                    ) {
                int row = h.getRow();
                int col = h.getColumn();
                totalSeat += row*col;
            }

            //统计观众人次    只有已支付的人才会被统计
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = simpleDateFormat.parse(simpleDateFormat.format(date));
            List<ScheduleAudience> scheduleAudiences = statisticsMapper.selectScheduleAudience(date);
            int totalAudience = 0;
            for(ScheduleAudience s: scheduleAudiences){
                totalAudience += s.getAudience();
            }

            //计算与转换
            List<MoviePlacingRateVO> moviePlacingRateVOList = new ArrayList<MoviePlacingRateVO>();
            MoviePlacingRate po = new MoviePlacingRate();
            for (ScheduleAudience scheduleAudience: scheduleAudiences
                    ) {
                String movieName = statisticsMapper.selectMovieName(scheduleAudience.getMovieId());
                po.setMovieName(movieName);

                double
                po.setMoviePlacingRate();
            }
            double placingRate = scheduleAudiences.size()/totalAudience/totalSeat/statisticsHall.size();
            DecimalFormat df = new DecimalFormat("0.00%");
            String str_moviePlacingRate = df.format(placingRate);

            MoviePlacingRate moviePlacingRate = new MoviePlacingRate();
            moviePlacingRate.setMoviePlacingRate(str_moviePlacingRate);
            MoviePlacingRateVO moviePlacingRateVO = new MoviePlacingRateVO(moviePlacingRate);
            return ResponseVO.buildSuccess(moviePlacingRateVO);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }
    @Override
    public ResponseVO getMoviePlacingRateByDate(Date date) {
        try{List<MoviePlacingRateVO> moviePlacingRateVOList = new ArrayList<MoviePlacingRateVO>();
        MoviePlacingRate po = new MoviePlacingRate();
        po.setMovieName("夏目友人帐");
        po.setMoviePlacingRate("20.05%");
        moviePlacingRateVOList.add(new MoviePlacingRateVO(po));
        po.setMovieName("惊奇队长");
        po.setMoviePlacingRate("30.96%");
        moviePlacingRateVOList.add(new MoviePlacingRateVO(po));
        return ResponseVO.buildSuccess(moviePlacingRateVOList);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("得到上座率失败");
        }
    }

    @Override
    public ResponseVO getPopularMovies(int days, int movieNum) {
        //要求见接口说明
    	try {
    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            Date startDate = getNumDayAfterDate(today, -days);
            
            return ResponseVO.buildSuccess(movieBoxTotalOfficeList2MovieBoxOfficeVOArray(statisticsMapper.selectRecentMovieBoxOffice(startDate), movieNum)); 
    	}
    	catch (Exception e) {
    		e.printStackTrace();
            return ResponseVO.buildFailure("失败");
    	}
    }

    
	/**
     * 获得num天后的日期
     * @param oldDate
     * @param num
     * @return
     */
    Date getNumDayAfterDate(Date oldDate, int num){
        Calendar calendarTime = Calendar.getInstance();
        calendarTime.setTime(oldDate);
        calendarTime.add(Calendar.DAY_OF_YEAR, num);
        return calendarTime.getTime();
    }


    private List<MovieScheduleTimeVO> movieScheduleTimeList2MovieScheduleTimeVOList(List<MovieScheduleTime> movieScheduleTimeList){
        List<MovieScheduleTimeVO> movieScheduleTimeVOList = new ArrayList<>();
        for(MovieScheduleTime movieScheduleTime : movieScheduleTimeList){
            movieScheduleTimeVOList.add(new MovieScheduleTimeVO(movieScheduleTime));
        }
        return movieScheduleTimeVOList;
    }


    private List<MovieTotalBoxOfficeVO> movieTotalBoxOfficeList2MovieTotalBoxOfficeVOList(List<MovieTotalBoxOffice> movieTotalBoxOfficeList){
        List<MovieTotalBoxOfficeVO> movieTotalBoxOfficeVOList = new ArrayList<>();
        for(MovieTotalBoxOffice movieTotalBoxOffice : movieTotalBoxOfficeList){
            movieTotalBoxOfficeVOList.add(new MovieTotalBoxOfficeVO(movieTotalBoxOffice));
        }
        return movieTotalBoxOfficeVOList;
    }
    
    
    private MovieTotalBoxOfficeVO[] movieBoxTotalOfficeList2MovieBoxOfficeVOArray(List<MovieTotalBoxOffice> movieTotalBoxOfficeList, int movieNum) {
    	MovieTotalBoxOfficeVO[] movieTotalBoxOfficeVOArray = new MovieTotalBoxOfficeVO[movieNum];
    	for(int i = 0; i < movieNum; i++) {
    		movieTotalBoxOfficeVOArray[i] = new MovieTotalBoxOfficeVO(movieTotalBoxOfficeList.get(i));
    	}
		return movieTotalBoxOfficeVOArray;
	}
}
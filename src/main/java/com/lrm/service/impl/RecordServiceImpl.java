package com.lrm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lrm.dao.RecordRepository;
import com.lrm.po.Record;
import com.lrm.service.RecordService;
import com.lrm.util.HttpClient;
import com.lrm.util.IPUtils;
import com.lrm.util.PushWechatMessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class RecordServiceImpl implements RecordService {

    @Autowired
    private RecordRepository recordRepository;

    @Override
    public List<Record> findByAddressLike(String address) {
        return recordRepository.findByAddressLike(address);
    }

    @Override
    public void recording(HttpServletRequest httpServletRequest) {

        //获取用户 IP 地址
        String ip = IPUtils.getIpAddr(httpServletRequest);

        Record record = recordRepository.findByIp(ip);

        if(null == record){
            record = new Record();
            record.setIp(ip);
            record.setLastVisitTime(new Date());
            record.setTotalNumberOfVisits(new Long(1));
            String getAddressByIpRequestUrl = "https://www.maitube.com/ip/?ip="+record.getIp();

            String result = HttpClient.doGet(getAddressByIpRequestUrl);

            record.setAddress(result.substring(result.indexOf(":")+1,result.length()-1));

            Record saveRecord =  recordRepository.save(record);

//            /*推送微信消息*/
//            String title = "新的IP地址访问通知";
//            String content =
//                    "访问记录ID:" + saveRecord.getId()+
//                    "<br>访问IP地址:"+record.getIp()+
//                    "<br>访问地区:"+record.getAddress()+
//                    "<br>访问时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//            PushWechatMessageUtil.pushMessageByPost(title,content);
//
        }else {
            record.setLastVisitTime(new Date());
            record.setTotalNumberOfVisits(record.getTotalNumberOfVisits()+1);
            recordRepository.save(record);
        }

    }

    @Override
    public List<Record> getAll() {
        List<Record> recordList = recordRepository.findAll();

        Collections.sort(recordList,new Comparator<Record>() {

            @Override
            public int compare(Record o1, Record o2) {
                return (o1.getLastVisitTime().after(o2.getLastVisitTime())) ? -1 : 0;
            }
        });
        return recordList;
    }

    @Override
    public Record findByIp(String ip) {
        return recordRepository.findByIp(ip);
    }
}

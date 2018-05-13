package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dao.StatusSuratMapper;
import com.example.model.StatusSuratModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StatusSuratServiceDatabase implements StatusSuratService {
	@Autowired
	private StatusSuratMapper statusSuratMapper;
	
	@Override
	public StatusSuratModel selectStatusSurat(int id_status_surat) {
		log.info("select * from status surat with id: ", id_status_surat);
		return statusSuratMapper.selectStatusSurat(id_status_surat);
	}
	
	@Override
	public String getStatusSurat(int id_status_surat) {
		log.info ("select letter with id_status_surat {}", id_status_surat);
		return statusSuratMapper.getStatusSurat(id_status_surat);
	}

}

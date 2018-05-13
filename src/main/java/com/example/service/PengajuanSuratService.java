package com.example.service;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.model.PengajuanSuratModel;

public interface PengajuanSuratService {
	 List<PengajuanSuratModel> selectAllPengajuan();
	 PengajuanSuratModel getStatusSurat(String no_surat);
	 List<PengajuanSuratModel> selectPengajuan (String username_pengaju);
	 PengajuanSuratModel getDetailPengajuanSurat(int id_pengajuan_surat);
	 List<PengajuanSuratModel> selectPengajuanByDate (String startDate, String endDate);
	 List<PengajuanSuratModel> selectPengajuanByDateMahasiswa (String startDate, String endDate, String name);
	 List<PengajuanSuratModel> selectPengajuanByStatus (String status);
	 List<PengajuanSuratModel> selectPengajuanByStatusMahasiswa (String status, String nama);
	 List<PengajuanSuratModel> selectAllStatus ();
	 void updateStatusPengajuanSurat(int id_pengajuan_surat, int id_status, String pegawai);
	 List<PengajuanSuratModel> selectAllPengajuanFilterByJenis (int id_jenis_surat, String name);

	 List<PengajuanSuratModel> selectAllPengajuanFilterByJenisMahasiswa (int id_jenis_surat, String name);
	 void addPengajuanSurat (PengajuanSuratModel pengajuanSuratModel);
	 String selectNo_surat();
	 int getCountProcessedSurat(int npm);
	 int getCountFinishedSurat(int npm);
	 void updateStatusUpload(String id_pengajuan_surat);
}

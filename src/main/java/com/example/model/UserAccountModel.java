package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountModel {
	private String username;
	private String password;
	private String role;
	private MahasiswaModel mhs;
	private PegawaiModel pegawai;
}

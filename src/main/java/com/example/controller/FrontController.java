package com.example.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.model.JenisSuratModel;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.model.JenisSuratModel;
import com.example.model.MahasiswaModel;
import com.example.model.MataKuliahModel;
import com.example.model.PegawaiModel;
import com.example.model.PengajuanSuratModel;
import com.example.service.JenisSuratService;
import com.example.service.MahasiswaService;
import com.example.service.MataKuliahService;
import com.example.service.PegawaiService;
import com.example.service.PengajuanSuratService;
import com.example.service.StatusSuratService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class FrontController {
	
    @Autowired
    MahasiswaService studentDAO;
    
    @Autowired
	PengajuanSuratService pengajuanSuratDAO;
    
    @Autowired
    PegawaiService pegawaiDAO;
    
    @Autowired
    JenisSuratService jenisSuratDAO;
    
    @Autowired
    StatusSuratService statusSuratDAO;
    
    @Autowired
    MataKuliahService matkulDAO;
    
    @Autowired
    ServletContext context;
    
	@RequestMapping("/")
	public String index(Model model) {
		return "index";
	}
	
	@RequestMapping("/default")
    public String defaultAfterLogin(HttpServletRequest request) {
        if (request.isUserInRole("MAHASISWA")) {
            return "redirect:/pengajuan/riwayat";
        }
        return "redirect:/pengajuan/viewall";
    }
	
    @RequestMapping("/pengajuan/riwayat")
    public String viewPengajuanSurat (Model model)
    {
    	List<JenisSuratModel> allJenisSurat = jenisSuratDAO.selectAllJenisSurat();
    	List<PengajuanSuratModel> lstStatus = pengajuanSuratDAO.selectAllStatus();
    	String namaMahasiswa, namaPegawai;
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
    	
    	List<PengajuanSuratModel> letter = pengajuanSuratDAO.selectPengajuan(name);
    	
    	for(int i=0;i<letter.size();i++) {
    		namaMahasiswa = searchName(letter.get(i).getUsername_pengaju());
    		letter.get(i).setUsername_pengaju(namaMahasiswa);
    		log.info("nama ="+letter.get(i).getUsername_pegawai());
    		if(letter.get(i).getUsername_pegawai() == null) {
    			log.info("test");
    			namaPegawai = "Not assigned";
    		}else {
    			namaPegawai = searchNamaPegawai(letter.get(i).getUsername_pegawai());
    		}
    		letter.get(i).setUsername_pegawai(namaPegawai);
    	} 
    	model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
    	model.addAttribute("letter", letter);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	model.addAttribute("lstStatus", lstStatus);
    	return "riwayatSurat";
    }
    
    @RequestMapping("/pengajuan/riwayat/filterByDate/{tanggalAwal}/{tanggalAkhir}")
    public String filterByDateMahasiswa(Model model, @PathVariable(value = "tanggalAwal") String tanggalAwal, @PathVariable(value="tanggalAkhir") String tanggalAkhir) {
    	String namaMahasiswa, namaPegawai, employeeName;
    	log.info("awal "+tanggalAwal);
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
    	log.info("user logged in"+ name);
    	List<JenisSuratModel> allJenisSurat = jenisSuratDAO.selectAllJenisSurat();
    	List<PengajuanSuratModel> lstSurat = pengajuanSuratDAO.selectPengajuanByDateMahasiswa(tanggalAwal, tanggalAkhir, name);
    	List<PengajuanSuratModel> lstStatus = pengajuanSuratDAO.selectAllStatus();
    	for(int i=0;i<lstSurat.size();i++) {
    		namaMahasiswa = searchName(lstSurat.get(i).getUsername_pengaju());
    		employeeName = lstSurat.get(i).getUsername_pegawai();
    		
    		if(employeeName != null) {
    			namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		}else {
    			namaPegawai = "Not assigned";
    		}
    		namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		System.out.println("nama pegawai "+namaPegawai);
    		lstSurat.get(i).setUsername_pengaju(namaMahasiswa);
    		lstSurat.get(i).setUsername_pegawai(namaPegawai);
    	}
    	model.addAttribute("letter", lstSurat);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	model.addAttribute("lstStatus", lstStatus);
    	return "riwayatSurat";
    }
    
    @RequestMapping("/pengajuan/riwayat/filterByStatus/{status}")
    public String filterByStatusMahasiswa(Model model, @PathVariable(value = "status") String status) {
    	String namaMahasiswa, namaPegawai;
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
    	log.info("user logged in"+ name);
    	List<PengajuanSuratModel> lstSurat = pengajuanSuratDAO.selectPengajuanByStatusMahasiswa(status, name);
    	List<PengajuanSuratModel> lstStatus = pengajuanSuratDAO.selectAllStatus();
    	List<JenisSuratModel> allJenisSurat = jenisSuratDAO.selectAllJenisSurat();
    	for(int i=0;i<lstSurat.size();i++) {
    		namaMahasiswa = searchName(lstSurat.get(i).getUsername_pengaju());
    		String employeeName;
			employeeName = lstSurat.get(i).getUsername_pegawai();
    		
    		if(employeeName != null) {
    			namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		}else {
    			namaPegawai = "Not assigned";
    		}		
    		namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		System.out.println("nama pegawai "+namaPegawai);
    		lstSurat.get(i).setUsername_pengaju(namaMahasiswa);
    		lstSurat.get(i).setUsername_pegawai(namaPegawai);
    	} 
    	model.addAttribute("letter", lstSurat);
    	model.addAttribute("lstStatus", lstStatus);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
    	return "riwayatSurat";
    }
    
    @RequestMapping("pengajuan/riwayat/filterByJenis/{jenis}")
    public String filterByJenisMahasiswa(Model model, @PathVariable(value = "jenis") int jenisSurat) {
		List<JenisSuratModel> allJenisSurat = jenisSuratDAO.selectAllJenisSurat();
		List<PengajuanSuratModel> lstStatus = pengajuanSuratDAO.selectAllStatus();
    	String namaMahasiswa, namaPegawai;
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
    	List<PengajuanSuratModel> lstSurat = pengajuanSuratDAO.selectAllPengajuanFilterByJenisMahasiswa(jenisSurat, name);
    	for(int i=0;i<lstSurat.size();i++) {
    		namaMahasiswa = searchName(lstSurat.get(i).getUsername_pengaju());
    		String employeeName;
			employeeName = lstSurat.get(i).getUsername_pegawai();
    		
    		if(employeeName != null) {
    			namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		}else {
    			namaPegawai = "Not assigned";
    		}
    		namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		lstSurat.get(i).setUsername_pengaju(namaMahasiswa);
    		lstSurat.get(i).setUsername_pegawai(namaPegawai);
    	} 
    	model.addAttribute("lstSurat", lstSurat);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	model.addAttribute("lstStatus", lstStatus);
    	model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
    	return "riwayatSurat";
    }
    
    @RequestMapping("/pengajuan/viewall")
    public String viewAllPengajuanSurat(Model model) {
    	List<JenisSuratModel> allJenisSurat = jenisSuratDAO.selectAllJenisSurat();
    	String namaMahasiswa, namaPegawai;
    	List<PengajuanSuratModel> lstSurat = pengajuanSuratDAO.selectAllPengajuan();
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth.getName();

    	List<PengajuanSuratModel> lstStatus = pengajuanSuratDAO.selectAllStatus();

    	for(int i=0;i<lstSurat.size();i++) {
    		namaMahasiswa = searchName(lstSurat.get(i).getUsername_pengaju());
    		if(lstSurat.get(i).getUsername_pegawai() == null) {
    			namaPegawai = "Not Assigned";
    		} else {
    			namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		}
    		
    		System.out.println("nama pegawai "+namaPegawai);
    		lstSurat.get(i).setUsername_pengaju(namaMahasiswa);
    		lstSurat.get(i).setUsername_pegawai(namaPegawai);
    		
    	} 
    	model.addAttribute("lstSurat", lstSurat);
    	model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
    	model.addAttribute("lstStatus", lstStatus);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	return "viewAllPengajuanSurat";
    }
    
    @RequestMapping("/pengajuan/viewall/filterByDate/{tanggalAwal}/{tanggalAkhir}")
    public String filterByDate(Model model, @PathVariable(value = "tanggalAwal") String tanggalAwal, @PathVariable(value="tanggalAkhir") String tanggalAkhir) {
    	String namaMahasiswa, namaPegawai;
    	log.info("awal "+tanggalAwal);
    	List<PengajuanSuratModel> lstSurat = pengajuanSuratDAO.selectPengajuanByDate(tanggalAwal, tanggalAkhir);
    	List<PengajuanSuratModel> lstStatus = pengajuanSuratDAO.selectAllStatus();
    	List<JenisSuratModel> allJenisSurat = jenisSuratDAO.selectAllJenisSurat();
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth.getName();
    	for(int i=0;i<lstSurat.size();i++) {
    		namaMahasiswa = searchName(lstSurat.get(i).getUsername_pengaju());
    		String employeeName;
			employeeName = lstSurat.get(i).getUsername_pegawai();
    		
    		if(employeeName != null) {
    			namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		}else {
    			namaPegawai = "Not assigned";
    		}
    		namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		System.out.println("nama pegawai "+namaPegawai);
    		lstSurat.get(i).setUsername_pengaju(namaMahasiswa);
    		lstSurat.get(i).setUsername_pegawai(namaPegawai);
    	}
    	model.addAttribute("lstSurat", lstSurat);
    	model.addAttribute("lstStatus", lstStatus);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
    	return "viewAllPengajuanSurat";
    }
    
    @RequestMapping("/pengajuan/viewall/filterByStatus/{status}")
    public String filterByStatus(Model model, @PathVariable(value = "status") String status) {
    	String namaMahasiswa, namaPegawai;
    	List<PengajuanSuratModel> lstSurat = pengajuanSuratDAO.selectPengajuanByStatus(status);
    	List<PengajuanSuratModel> lstStatus = pengajuanSuratDAO.selectAllStatus();
    	List<JenisSuratModel> allJenisSurat = jenisSuratDAO.selectAllJenisSurat();
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth.getName();
    	for(int i=0;i<lstSurat.size();i++) {
    		namaMahasiswa = searchName(lstSurat.get(i).getUsername_pengaju());
    		String employeeName;
			employeeName = lstSurat.get(i).getUsername_pegawai();
    		
    		if(employeeName != null) {
    			namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		}else {
    			namaPegawai = "Not assigned";
    		}
    		
    		namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		System.out.println("nama pegawai "+namaPegawai);
    		lstSurat.get(i).setUsername_pengaju(namaMahasiswa);
    		lstSurat.get(i).setUsername_pegawai(namaPegawai);
    	} 
    	model.addAttribute("lstSurat", lstSurat);
    	model.addAttribute("lstStatus", lstStatus);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
    	return "viewAllPengajuanSurat";
    }
    
    public String searchName(String npm) {
    	MahasiswaModel lstMahasiswa = studentDAO.selectMahasiswaByNPM(npm);
    	return lstMahasiswa.getNama();
    }
    
    public String searchNamaPegawai(String nip) {
    	log.info("pegawai "+ nip);
    	PegawaiModel lstPegawai = pegawaiDAO.selectPegawaiByNIP(nip);
    	return lstPegawai.getNama();
    }
    
    @RequestMapping("/login")
	public String login() {
		return "index";
    }
    
	@RequestMapping("/listSurat")
	public String listSurat() {
		return "listSurat";
	}
	
	@RequestMapping("/pengajuan/view/{id_pengajuan_surat}")
	public String detailPengajuanSurat(Model model, @PathVariable(value = "id_pengajuan_surat") int id_pengajuan_surat) {
		PengajuanSuratModel surat = pengajuanSuratDAO.getDetailPengajuanSurat(id_pengajuan_surat);
		MataKuliahModel matkul = matkulDAO.getMatakuliahById(surat.getId_matkul_terkait());
		String npm = surat.getUsername_pengaju();
		String nama_pegawai;
		
		if(surat.getUsername_pegawai() == null) {
			nama_pegawai = "Not Assigned";
		} else {
			nama_pegawai = this.searchNamaPegawai(surat.getUsername_pegawai());
		}
		log.info("ini pegawai di view "+ nama_pegawai);
		model.addAttribute("surat", surat);
		model.addAttribute("nama", this.searchName(npm));
		model.addAttribute("jenis_surat", jenisSuratDAO.selectJenisSurat(surat.getId_jenis_surat()).getNama());
		model.addAttribute("nama_admin", nama_pegawai);
		model.addAttribute("status_surat", statusSuratDAO.getStatusSurat(surat.getId_status_surat()));
		model.addAttribute("nama_matkul", matkul.getNama_matkul());
		model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(npm)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(npm)));
		return "detailPengajuanSurat";
	}

	@RequestMapping("/pengajuan/ubah/{id_pengajuan_surat}/{id_status}")
	public String updateStatusSurat(Model model, 
			@PathVariable(value = "id_pengajuan_surat") int id_pengajuan_surat, 
			@PathVariable(value = "id_status") int id_status, HttpServletRequest request) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth.getName();
		log.info("ini id status "+ id_status);
		log.info("ini name pegawai "+ name);
		pengajuanSuratDAO.updateStatusPengajuanSurat(id_pengajuan_surat, id_status, name);
		String referer = request.getHeader("Referer");
		return "redirect:"+ referer;
	}
	
	
	@RequestMapping("/pengajuan/tambah")
	public String ajukanSurat(Model model) {
		PengajuanSuratModel pengajuanSurat = new PengajuanSuratModel();
		List<JenisSuratModel> jenisSurat = jenisSuratDAO.getAllJenisSurat();
		List<MataKuliahModel> mata_kuliah = matkulDAO.getAllMatakuliah();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth.getName();
		model.addAttribute("pengajuanSurat", pengajuanSurat);
    	model.addAttribute("jenis_surats", jenisSurat);
    	model.addAttribute("mata_kuliah", mata_kuliah);
    	model.addAttribute("url","tambah");
    	model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
    	return "ajukanSurat";
	}
	
	@RequestMapping("/pengajuan/tambah/submit")
	public String ajukanSuratSubmit(@ModelAttribute PengajuanSuratModel pengajuanSurat, Model model, HttpServletRequest request, RedirectAttributes ra) {
		Date date = new Date();
		String referer = request.getHeader("Referer");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth.getName();
		MahasiswaModel mahasiswa = studentDAO.selectMahasiswaByNPM(name);
		int idMhs = mahasiswa.getId();
		String nomorSurat = generateNomorSurat(pengajuanSuratDAO.selectNo_surat());
		pengajuanSurat.setNo_surat(nomorSurat);
		log.info("Nomor Surat "+ nomorSurat);
		pengajuanSurat.setUsername_pengaju(name);
		pengajuanSurat.setTgl_mohon(date);
		pengajuanSurat.setId_status_surat(1);
		pengajuanSurat.setStatus_upload(0);
		
		model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
        
    	
		if(pengajuanSurat.getId_jenis_surat() == 3) {
			if(studentDAO.selectMahasiswaAsdosByNPM(idMhs) == true) {
				pengajuanSuratDAO.addPengajuanSurat(pengajuanSurat);
				String msg = "Pengajuan surat Anda berhasil disimpan";
				model.addAttribute("sukses", msg);
				return "riwayatSurat";
			}else {
				String msg = "Maaf, hanya asdos yang bisa mengajukan Surat Keterangan Asisten Dosen";
				ra.addFlashAttribute("error", msg);
				return "redirect:" + referer;
			}
		}else {
			pengajuanSuratDAO.addPengajuanSurat(pengajuanSurat);
			String msg = "Pengajuan surat Anda berhasil disimpan";
			model.addAttribute("sukses", msg);
			return "riwayatSurat";
		}
		
	}
	
	public String generateNomorSurat(String no_surat) {
    	String newNoSurat = "";
    	log.info("Nomor Surat "+ no_surat);
    	if (no_surat != null) {
	    	int nomor_urut = Integer.parseInt(no_surat);
    		nomor_urut = nomor_urut + 1 ;
    		newNoSurat = "0"+String.valueOf(nomor_urut);
    	}else {
    		newNoSurat = "001";
    	}
    	return newNoSurat;
    }

	@RequestMapping(value="/pengajuan/upload", method=RequestMethod.POST, consumes = "multipart/form-data")
    public String handleFileUpload(@RequestBody MultipartFile[] file, HttpServletRequest request, RedirectAttributes ra){
		String referer = request.getHeader("Referer");
		String split[] = referer.split("\\/");
		String id_surat = split[split.length-1];
		String contentType = file[0].getContentType();
		
		log.info("file content type "+file[0].getContentType());
		
		if(!contentType.equals("application/pdf")) {
			log.info("masuk sini");
			String msg = "Gagal! Hanya file format PDF yang bisa di upload!";
            ra.addFlashAttribute("error", msg);
            return "redirect:"+referer;
		}
		String rootPath = System.getProperty("user.dir");
		String file_location = rootPath + File.separator + "src"+File.separator+"main"+File.separator+"resources"+File.separator+"uploads"+File.separator;
	
		 if (file[0].isEmpty()) {
			 String msg = "File kosong";
	            ra.addFlashAttribute("error", msg);
	            return "redirect:"+referer;
	        }

	        try {
	            byte[] bytes = file[0].getBytes();
	            Path path = Paths.get(file_location+  id_surat);
	            log.info(path.toString());
	            Files.write(path, bytes);
	            pengajuanSuratDAO.updateStatusUpload(id_surat);
	            String msg = "Berkas surat berhasil di unggah.";
	            ra.addFlashAttribute("sukses", msg);

	        } catch (IOException e) {
	        	
	            e.printStackTrace();
	            String msg = "Terdapat kesalahan System. Coba beberapa saat lagi!";
	            ra.addFlashAttribute("error", msg);
	        }
		
	        return "redirect:"+referer;
    }
	
	@RequestMapping(value = "/pengajuan/download/{id_pengajuan_surat}", method = RequestMethod.GET)
	public ResponseEntity<Resource> download(String param, @PathVariable(value = "id_pengajuan_surat") int id_pengajuan_surat) throws IOException {
		
		String rootPath = System.getProperty("user.dir");
		String file_location = rootPath + File.separator + "src"+File.separator+"main"+File.separator+"resources"+File.separator+"uploads"+File.separator;
		String fileName = id_pengajuan_surat+".pdf";
		
		try {
			File downloadedFile = new File(file_location+id_pengajuan_surat);
	    	Path path = Paths.get(downloadedFile.getAbsolutePath());
	        HttpHeaders headers = new HttpHeaders();
	        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName);
	        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
	        headers.add("Pragma", "no-cache");
	        headers.add("Expires", "0");
	        System.out.println("The length of the file is : "+downloadedFile.length());
	        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
	        
			if(downloadedFile.exists()) {
			    return ResponseEntity.ok()
			            .headers(headers)
			            .contentLength(downloadedFile.length())
			            .contentType(MediaType.parseMediaType("application/octet-stream"))
			            .body(resource);
			}else {
				 return ResponseEntity.status(HttpStatus.NOT_FOUND)
				            .headers(headers)
				            .contentLength(downloadedFile.length())
				            .contentType(MediaType.parseMediaType("application/octet-stream"))
				            .body(resource);
			}  
		}catch (IOException e){
			return null;
		}	
	}
	
	@RequestMapping("pengajuan/viewall/filterByJenis/{jenis}")
    public String filterByJenis(Model model, @PathVariable(value = "jenis") int jenisSurat) {
		List<JenisSuratModel> allJenisSurat = jenisSuratDAO.selectAllJenisSurat();
		List<PengajuanSuratModel> lstStatus = pengajuanSuratDAO.selectAllStatus();

    	String namaMahasiswa, namaPegawai;
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
    	List<PengajuanSuratModel> lstSurat = pengajuanSuratDAO.selectAllPengajuanFilterByJenis(jenisSurat, name);
    	for(int i=0;i<lstSurat.size();i++) {
    		namaMahasiswa = searchName(lstSurat.get(i).getUsername_pengaju());
    		String employeeName;
			employeeName = lstSurat.get(i).getUsername_pegawai();
    		
    		if(employeeName != null) {
    			namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		}else {
    			namaPegawai = "Not assigned";
    		}
    		
    		namaPegawai = searchNamaPegawai(lstSurat.get(i).getUsername_pegawai());
    		lstSurat.get(i).setUsername_pengaju(namaMahasiswa);
    		lstSurat.get(i).setUsername_pegawai(namaPegawai);
    	} 
    	model.addAttribute("lstSurat", lstSurat);
    	model.addAttribute("jenisSurat", allJenisSurat);
    	model.addAttribute("lstStatus", lstStatus);
    	model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
    	return "viewAllPengajuanSurat";
    }
  
	@RequestMapping("/pengajuan/riwayat/{id_pengajuan_surat}")
	public String detailRiwayatPengajuanSurat(Model model, @PathVariable(value = "id_pengajuan_surat") int id_pengajuan_surat) {
		PengajuanSuratModel letter1 = pengajuanSuratDAO.getDetailPengajuanSurat(id_pengajuan_surat);
		MataKuliahModel matkul = matkulDAO.getMatakuliahById(letter1.getId_matkul_terkait());
		String npm = letter1.getUsername_pengaju();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth.getName(); //get logged in username
		String nama_pegawai;
		if(letter1.getUsername_pegawai() == null) {
			
			nama_pegawai = "Not Assigned";
		} else {
			nama_pegawai = this.searchNamaPegawai(letter1.getUsername_pegawai());
		}
		
		log.info("ini status surat "+statusSuratDAO.getStatusSurat(letter1.getId_status_surat()));
		model.addAttribute("letter1", letter1);
		model.addAttribute("nama", this.searchName(npm));
		model.addAttribute("jenis_surat", jenisSuratDAO.selectJenisSurat(letter1.getId_jenis_surat()).getNama());
		model.addAttribute("nama_admin", nama_pegawai);
		model.addAttribute("status_surat", statusSuratDAO.getStatusSurat(letter1.getId_status_surat()));
		model.addAttribute("nama_matkul", matkul.getNama_matkul());
		model.addAttribute("finished_surat", pengajuanSuratDAO.getCountFinishedSurat(Integer.parseInt(name)));
    	model.addAttribute("processed_surat", pengajuanSuratDAO.getCountProcessedSurat(Integer.parseInt(name)));
		return "riwayatSuratDetail";
	}
}

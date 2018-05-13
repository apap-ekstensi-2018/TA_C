# Aplikasi SISURAT Kelompok C

Base url: `https://kelompok-c.herokuapp.com`

API List:
* [api/surat/view/{no_surat}]

## api/surat/view/{no_surat}

Mengembalikan data apakah pengajuan surat berjenis Surat Pengantar Peminjaman Buku yang telah dilakukan sudah selesai diproses atau belum.

**URL** : `api/surat/view/{no_surat}`

**Method** : `GET`

### Response 200

**Contoh Request**: [/api/surat/view/{no_surat}](https://kelompok-c.herokuapp.com//api/surat/view/001)

```json
[
  {
  "Status": "200",
  "Message": "Data ditemukan",
  "Surat": {
    "id_mahasiswa": 3,
    "jenis_surat": "Surat Pengantar Peminjaman Buku",
    "keterangan_surat": "Peminjaman Buku Software Architecture",
    "status_surat": "Diproses"
  }
}
]
```

### Response 404

**Contoh Request**: [/api/surat/view/{no_surat}](https://kelompok-c.herokuapp.com//api/surat/view/987)

```json
[
{
  "Status": "404",
  "Message": "Data tidak ditemukan"
}
]
```
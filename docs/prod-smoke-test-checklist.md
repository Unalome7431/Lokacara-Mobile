# Prod Smoke Test Checklist

Gunakan checklist ini setelah backend prod di-deploy dan Android diarahkan ke environment yang sama.

## Prerequisites

- Pastikan app Android memakai base URL production.
- Siapkan 1 akun organizer dan 1 akun peserta test.
- Siapkan minimal 1 event offline test yang punya attendee berstatus `present`.
- Siapkan 1 template sertifikat valid untuk event test.

## 1. Login

- Login sebagai organizer.
- Pastikan dashboard, home feed, dan profile terbuka tanpa error.
- Expected:
  - tidak ada crash
  - data user muncul normal

## 2. Create Event Offline

- Buka create event.
- Pilih mode offline.
- Isi nama, deskripsi, kategori, waktu mulai, waktu selesai, harga, kuota.
- Pilih lokasi dari map.
- Jangan isi city manual.
- Submit event.
- Expected:
  - event berhasil dibuat
  - tidak kena `422` karena field `city`
  - event tersimpan dengan lokasi lengkap

## 3. Edit Event Offline

- Buka event offline yang baru dibuat.
- Ubah satu field kecil, misalnya judul atau alamat.
- Simpan perubahan.
- Expected:
  - update berhasil
  - city tetap aman tanpa input manual

## 4. Certificate State

- Buka halaman certificate management untuk event yang punya attendee `present`.
- Expected:
  - state sertifikat muncul dari backend
  - payload flat terbaca: `is_eligible`, `has_template`, `issued_count`, `last_issued_at`, `status`, `layout`
  - tidak jatuh ke fallback lokal jika endpoint backend tersedia

## 5. Template Upload

- Upload template sertifikat untuk event test.
- Expected:
  - template tersimpan untuk event itu saja
  - tidak menimpa template event lain

## 6. Distribute Certificates

- Jalankan distribusi sertifikat.
- Expected:
  - status berubah sesuai backend
  - issued count bertambah
  - file sertifikat peserta bisa diunduh dari menu terkait

## 7. Search by Location

- Cari event lewat lokasi/city yang sama.
- Expected:
  - hasil yang tampil hanya event dengan city yang cocok
  - tidak ada venue-name false positive

## Pass Criteria

- Semua flow di atas selesai tanpa `422`, crash, atau mismatch parsing.
- Certificate management membaca state backend.
- Create/update event offline berjalan tanpa city manual.

## Fail Criteria

- `422` pada create/update event offline.
- Certificate state gagal parse atau fallback lokal masih dipakai padahal backend tersedia.
- Template bisa berpindah antar event.
- Search location menampilkan event city yang salah.

package com.example.InfBezTim10.service;

import com.example.InfBezTim10.model.Certificate;
import com.example.InfBezTim10.model.User;

public interface ICertificateService extends IJPAService<Certificate>{
    Certificate findBySerialNumber(String serialNumber);
}

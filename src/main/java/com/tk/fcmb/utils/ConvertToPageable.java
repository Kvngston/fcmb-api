package com.tk.fcmb.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class ConvertToPageable {

    private int page;
    private int size;

    public ConvertToPageable(int page, int size) {
        this.page = page;
        this.size = size;
    }


    public Page<?> convertListToPage(List<?> data){
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(data, pageable, data.size());
    }
}

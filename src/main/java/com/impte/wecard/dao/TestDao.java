package com.impte.wecard.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestDao {
    int insert(@Param("numbers") List numbers);
}

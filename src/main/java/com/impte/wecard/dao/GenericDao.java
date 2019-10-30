package com.impte.wecard.dao;

import java.io.Serializable;
import java.util.List;

/**
 * 基于泛型的DAO父接口
 *
 * @param <E> 实现了Serializabel接口的实体类
 * @param <ID> 主键ID类型并不确定
 *
 * @author justZero
 * @since 2018-1-27 15:52:04
 */
public interface GenericDao<E extends Serializable, ID> {

    /**
     * 根据ID查询
     * @param id 实体类id属性值
     * @return E
     */
    E findById(ID id);

    /**
     * 根据name查询
     * @param name 实体类类似如name的属性值
     * @return List<E>
     */
    List<E> findByName(String name);

    /**
     * 查询所有
     * @return List<E>
     */
    List<E> findAll();

    /**
     * 插入一条记录
     * @param e 实体类实例
     * @return 数据库中相应表受影响的行数
     */
    int insert(E e);

    /**
     * 更新一条记录
     * @param e 实体类实例
     * @return 数据库中相应表受影响的行数
     */
    int update(E e);

    /**
     * 删除一条记录
     * @param e 实体类实例
     * @return 数据库中相应表受影响的行数
     */
    int delete(E e);

    /**
     * 删除一条记录
     * @param id 实体类id属性值
     * @return 数据库中相应表受影响的行数
     */
    int delete(ID id);

}

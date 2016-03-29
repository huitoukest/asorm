package com.tingfeng.asorm.entity;

/**
 * @author dview
 *
 */
public abstract class BaseEntity {
	public Long _id;
	
	@Override
	public boolean equals(Object o) {
		if(this.getClass().getName().equals(o.getClass().getName())&&this.getPrimaryKeyValue()-((BaseEntity)o).getPrimaryKeyValue()==0)
			return true;
		return super.equals(o);
	}
	@Override
	public int hashCode() {
		return getPrimaryKeyValue().intValue();
	}	
	/**
	 * 返回主键的值,只能是long类型
	 * @return
	 */
	public abstract Long getPrimaryKeyValue();
	/**
	 * 返回表名称
	 * @return
	 */
	public String getTableName(){
		String name=this.getClass().getSimpleName();
		return name;
	}
	/**
	 * 返回主键的名称
	 * @return
	 */
	public abstract String getPrimaryKeyName();
}

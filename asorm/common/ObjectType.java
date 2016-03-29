package com.tingfeng.asorm.common;

import java.lang.reflect.Field;

public enum ObjectType{
	Boolean,Date,Float,Double,Long,Integer,String,Short,Byte,Other;
	
	public static ObjectType getObjectType(Class<?> cls){
		return getObjectType(cls.getName());
	}
	public  static ObjectType getObjectType(String className){
		String type=className;
		switch (type) {
		case ObjectTypeString.clsNameBaseBoolean:
				return ObjectType.Boolean;
		case ObjectTypeString.clsNameBoolean:
				return ObjectType.Boolean;
		case ObjectTypeString.clsNameDate:
				return ObjectType.Date;
		case ObjectTypeString.clsNameBaseFloat:
			return ObjectType.Float;
	    case ObjectTypeString.clsNameFloat:
			return ObjectType.Float;
	    case ObjectTypeString.clsNameBaseDouble:
			return ObjectType.Double;
	    case ObjectTypeString.clsNameDouble:
			return ObjectType.Double;
	    case ObjectTypeString.clsNameBaseLong:
			return ObjectType.Long;
	    case ObjectTypeString.clsNameLong:
			return ObjectType.Long;
	    case ObjectTypeString.clsNameBaseInt:
			return ObjectType.Integer;
	    case ObjectTypeString.clsNameInteger:
			return ObjectType.Integer;
	    case ObjectTypeString.clsNameString:
			return ObjectType.String;
	    case ObjectTypeString.clsNameBaseShort:
			return ObjectType.Short;
	    case ObjectTypeString.clsNameShort:
			return ObjectType.Short;
	    case ObjectTypeString.clsNameBaseByte:
			return ObjectType.Byte;
	    case ObjectTypeString.clsNameByte:
			return ObjectType.Byte;
		default:
			break;
		}
		
		return Other;
	}
	public static ObjectType getObjectType(Field field){
		return getObjectType(field.getType().getCanonicalName());
	}

}

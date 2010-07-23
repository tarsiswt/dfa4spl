package br.ufal.cideei.soot.analyses;

import soot.Local;
import soot.Unit;

public class Definition
{
	private Unit defStatement;
	private Local local;
	
	public Local getLocal() {
		return local;
	}


	public void setLocal(Local local) {
		this.local = local;
	}
	
	public Definition(Local local, Unit statement)
	{
		this.local = local;
		this.defStatement = statement;
	}
	
	public Unit getDefStatement()
	{
		return this.defStatement;
	}
	
	public void setDefStatement(Unit statement)
	{
		this.defStatement = statement;
	}
	
	public boolean definesLocal(Local variable)
	{
		return this.local.equals(variable);
	}
	
	//@Override
//	public boolean equals(Object obj)
//	{
//		boolean ret = false;
//		if (obj != null)
//		{
//			if (obj instanceof Definition)
//			{
//				ret = this.field.equals(((Definition)obj).getField())
//						&& this.defStatement.equals(((Definition)obj).getDefStatement());
//			}
//		}
//		return ret;
//	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean ret = false;
		if (obj != null)
		{
			if (obj instanceof Definition)
			{
				ret = this.local.equals(((Definition)obj).getLocal())
				&& this.defStatement.equals(((Definition)obj).getDefStatement());
			}
		}
		return ret;
	}
	
	@Override
	public String toString()
	{
		return this.local.getType() + " : " + this.local.getName() + "  @{" + this.defStatement.toString() + "}";
		//return this.field.getSignature() + "@{" + this.defStatement.toString() + "}";
	}
}

package ru.bgcrm.plugin.bgbilling.proto.dao;

import org.bgerp.app.exception.BGException;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;

public class BillingModuleDAO
	extends BillingDAO
{
	protected int moduleId;

	protected int getModuleId()
    {
        return moduleId;
    }

    protected void setModuleId( int moduleId )
    {
        this.moduleId = moduleId;
    }

    public BillingModuleDAO( User user, DBInfo dbInfo, int moduleId )
		throws BGException
	{
		super( user, dbInfo );
		setModuleId( moduleId );
	}

	public BillingModuleDAO( User user, String billingId, int moduleId )
		throws BGException
	{
		super( user, billingId );
		setModuleId( moduleId );
	}
}

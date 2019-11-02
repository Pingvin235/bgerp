package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.AddressDistributionDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class AddressDistributionAction
extends BaseAction
{
	public static class Distribution
	{
		private final int id;
		private final String title;
		private final List<Integer> cityIds;
		private final Set<Integer> userGroupIds;
		private final boolean updateProcessExecutorOnDistrChange;
		private final Set<Integer> processTypeIds;
		private final Set<Integer> processStatusIds;
		private final int addressParamId;

		public Distribution( int id, String title, List<Integer> cityIds, Set<Integer> userGroupIds, boolean updateProcessExecutorOnDistrChange, Set<Integer> processTypeIds, Set<Integer> processStatusId, int addressParamId )
		{
			this.id = id;
			this.title = title;
			this.cityIds = cityIds;
			this.userGroupIds = userGroupIds;

			this.processTypeIds = processTypeIds;
			this.processStatusIds = processStatusId;
			this.addressParamId = addressParamId;
			this.updateProcessExecutorOnDistrChange = updateProcessExecutorOnDistrChange && this.processTypeIds.size() > 0 && processStatusId.size() > 0 && addressParamId > 0;
		}

		public int getId()
		{
			return id;
		}

		public String getTitle()
		{
			return title;
		}

		public List<Integer> getCityIds()
		{
			return cityIds;
		}

		public Set<Integer> getUserGroupIds()
		{
			return userGroupIds;
		}

		public boolean isUpdateProcessExecutorOnDistrChange()
		{
			return updateProcessExecutorOnDistrChange;
		}

		public Set<Integer> getProcessTypeIds()
		{
			return processTypeIds;
		}

		public Set<Integer> getProcessStatusIds()
		{
			return processStatusIds;
		}

		public int getAddressParamId()
		{
			return addressParamId;
		}
	}

	public static class QuarterDistribution
	{
		private final int id;
		private final String title;
		private final int cityId;
		private final List<Integer> groupIds;
		private boolean includeSubGroups;

		public QuarterDistribution( int id, String title, int cityId, List<Integer> groupIds, boolean includeSubGroups )
		{
			this.id = id;
			this.title = title;
			this.cityId = cityId;
			this.groupIds = groupIds;
			this.includeSubGroups = includeSubGroups;
		}

		public int getId()
		{
			return id;
		}

		public boolean isIncludeSubGroups()
		{
			return includeSubGroups;
		}

		public List<Integer> getGroupIds()
		{
			return groupIds;
		}

		public int getCityId()
		{
			return cityId;
		}

		public String getTitle()
		{
			return title;
		}
	}

	public static class Config
	extends ru.bgcrm.util.Config
	{
		private Map<Integer, Distribution> distrMap = new TreeMap<Integer, Distribution>();
		private Map<Integer, QuarterDistribution> quarterDistrMap = new TreeMap<Integer, QuarterDistribution>();

		public Config( ParameterMap setup )
		{
			super( setup );

			for( Map.Entry<Integer, ParameterMap> me : setup.subIndexed( "addressDistribution." ).entrySet() )
			{
				int id = me.getKey();
				ParameterMap params = me.getValue();

				Distribution distribution = new Distribution( id,
															  params.get( "title" ),
															  Utils.toIntegerList( params.get( "cityIds" ) ),
															  Utils.toIntegerSet( params.get( "userGroupIds" ) ),
															  params.getBoolean( "updateProcessExecutorOnDistrChange", false ),
															  Utils.toIntegerSet( params.get( "processTypeIds" ) ),
															  Utils.toIntegerSet( params.get( "processStatusId" ) ),
															  params.getInt( "addressParamId", 0 ) );

				if( distribution.getId() > 0 &&
					Utils.notBlankString( distribution.getTitle() ) &&
					distribution.getCityIds().size() > 0 )
				{
					distrMap.put( distribution.getId(), distribution );
				}
				else
				{
					log.error( "Error load distribution config " + id );
				}
			}

			for( Entry<Integer, ParameterMap> entry : setup.subIndexed( "quarterDistribution." ).entrySet() )
			{
				if( entry.getValue().containsKey( "title" ) )
				{
					int id = entry.getKey();
					String title = entry.getValue().get( "title" );
					int cityId = entry.getValue().getInt( "cityId", 0 );
					List<Integer> userGroupIds = Utils.toIntegerList( entry.getValue().get( "userGroupIds" ) );
					boolean includeSubGroups = entry.getValue().getBoolean( "includeSubGroups", false );

					if( id > 0 && Utils.notBlankString( title ) && cityId > 0 )
					{
						quarterDistrMap.put( entry.getKey(), new QuarterDistribution( id, title, cityId, userGroupIds, includeSubGroups ) );
					}
					else
					{
						log.error( "Error load quarter distribution config " + id );
					}
				}
			}
		}

		public Collection<Distribution> getDistributions()
		{
			return distrMap.values();
		}

		public Collection<QuarterDistribution> getQuarterDistributions()
		{
			return quarterDistrMap.values();
		}

		public Distribution getDistribution( int id )
		{
			return distrMap.get( id );
		}

		public QuarterDistribution getQuarterDistribution( int id )
		{
			return quarterDistrMap.get( id );
		}
	}

	public AddressDistributionAction()
	{
		super();
	}

	public ActionForward userList( ActionMapping mapping,
								   DynActionForm form,
								   HttpServletRequest request,
								   HttpServletResponse response,
								   Connection con )
	throws BGException
	{
		Config config = setup.getConfig( Config.class );
		Distribution dist = config.distrMap.get( form.getParamInt( "distrId", 0 ) );

		if( dist != null )
		{
			UserDAO userDAO = new UserDAO( con );

			List<User> users = new ArrayList<User>();
			if( dist != null )
			{
				users = userDAO.getUserList( dist.userGroupIds, form.getParam( "userMask" ) );
				//List<User> users = userDAO.getUserList( null, form.getParam( "userMask" ) );
			}

			form.getResponse().setData( "users", users );
		}

		return processUserTypedForward( con, mapping, form, response, "userList" );
	}

	public ActionForward undistributedHouses( ActionMapping mapping,
											  DynActionForm form,
											  HttpServletRequest request,
											  HttpServletResponse response,
											  Connection con )
	throws BGException
	{
		try
		{
			Config config = setup.getConfig( Config.class );
			Distribution dist = config.distrMap.get( form.getParamInt( "distrId", 0 ) );

			if( dist != null )
			{

				AddressDistributionDAO addressDistributionDAO = new AddressDistributionDAO( con );
				List<IdTitle> undistHouses = addressDistributionDAO.getUndistributedHouses( form.getParamInt( "distrId", -1 ), dist.getCityIds(), form.getParam( "quarterMask" ), form.getParam( "streetMask" ), form.getParam( "houseMask" ) );

				form.getResponse().setData( "undistHouses", undistHouses );
			}
		}
		catch( SQLException e )
		{
			throw new BGException();
		}

		return processUserTypedForward( con, mapping, form, response, "undistributedHouses" );
	}

	public ActionForward distrList( ActionMapping mapping,
									DynActionForm form,
									HttpServletRequest request,
									HttpServletResponse response,
									Connection con )
	{
		Config config = setup.getConfig( Config.class );

		form.getResponse().setData( "distrbution", config.distrMap );

		return processUserTypedForward( con, mapping, form, response, "distrList" );
	}

	public ActionForward userHouses( ActionMapping mapping,
									 DynActionForm form,
									 HttpServletRequest request,
									 HttpServletResponse response,
									 Connection con )
	throws BGException
	{
		try
		{
			AddressDistributionDAO addressDistributionDAO = new AddressDistributionDAO( con );

			List<IdTitle> userHouses = addressDistributionDAO.getUserHouses( form.getParamInt( "userId", -1 ), form.getParam( "quarterMask" ), form.getParam( "streetMask" ), form.getParam( "houseMask" ) );

			form.getResponse().setData( "userHouses", userHouses );

			int flat = addressDistributionDAO.getFlatCount( form.getParamInt( "userId" ) );

			form.getResponse().setData( "flat", flat );
		}
		catch( SQLException e )
		{
			throw new BGException();
		}

		return processUserTypedForward( con, mapping, form, response, "userHouses" );
	}

	public ActionForward attachHouseToUser( ActionMapping mapping,
											DynActionForm form,
											HttpServletRequest request,
											HttpServletResponse response,
											Connection con )
	throws BGException
	{
		try
		{
			int userId = form.getParamInt( "userId", -1 );
			int distrId = form.getParamInt( "distrId", -1 );
			String[] houseIds = form.getParamArray( "hid" );
			Config config = setup.getConfig( Config.class );
			Distribution distr = config.distrMap.get( distrId );

			AddressDistributionDAO addressDistributionDAO = new AddressDistributionDAO( con );
			addressDistributionDAO.addHouseToUser( distr, userId, houseIds );
		}
		catch( SQLException e )
		{
			throw new BGException();
		}

		return processUserTypedForward( con, mapping, form, response, "attachHouseToUser" );
	}

	public ActionForward removeHouseFromUser( ActionMapping mapping,
											  DynActionForm form,
											  HttpServletRequest request,
											  HttpServletResponse response,
											  Connection con )
	throws BGException
	{
		try
		{
			AddressDistributionDAO addressDistributionDAO = new AddressDistributionDAO( con );
			addressDistributionDAO.removeHouse( form.getParamInt( "userId", -1 ), form.getParamInt( "distrId", -1 ), form.getParamArray( "hid" ) );
		}
		catch( SQLException e )
		{
			throw new BGException();
		}

		return processUserTypedForward( con, mapping, form, response, "removeHouseFromUser" );
	}

	public ActionForward getHouses( ActionMapping mapping,
									DynActionForm form,
									HttpServletRequest request,
									HttpServletResponse response,
									Connection con )
	throws SQLException
	{
		Config config = setup.getConfig( Config.class );
		Distribution dist = config.distrMap.get( form.getParamInt( "distrId", 0 ) );

		if( dist != null )
		{
			AddressDistributionDAO addressDistributionDAO = new AddressDistributionDAO( con );
			List<Map<String, String>> houses = addressDistributionDAO.getAllHouses( dist.cityIds );

			form.getResponse().setData( "houses", houses );
		}

		return processUserTypedForward( con, mapping, form, response, "" );
	}

	public ActionForward getUserByHouse( ActionMapping mapping,
										 DynActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response,
										 Connection con )
	throws Exception
	{
		int houseId = form.getParamInt( "houseId" );
		int distrId = form.getParamInt( "distrId" );

		if( houseId > 0 && distrId > 0 )
		{
			AddressDistributionDAO distrDAO = new AddressDistributionDAO( con );
			User user = distrDAO.getUserByHouseId( distrId, houseId );
			if( user != null )
			{
				form.getResponse().setData( "id", user.getId() );
				form.getResponse().setData( "title", user.getTitle() );

				ParamValueDAO paramDAO = new ParamValueDAO( con );
				FileData fileData = paramDAO.getParamFile( user.getId(), form.getParamInt( "fileParamId" ), 1, 1 );

				form.getResponse().setData( "fileData", fileData );
			}
		}

		return processJsonForward( con, form, response );
	}

	public ActionForward getStreet( ActionMapping mapping,
									DynActionForm form,
									HttpServletRequest request,
									HttpServletResponse response,
									Connection con )
	throws BGException
	{
		try
		{
			Config config = setup.getConfig( Config.class );
			int distrId = form.getParamInt( "distrId" );
			if( distrId > 0 )
			{
				AddressDAO addressDAO = new AddressDAO( con );
				List<AddressItem> items = addressDAO.getAddressStreetsBySubstring( form.getParam( "street" ), config.distrMap.get( distrId ).cityIds );
				form.getResponse().setData( "items", items );
			}
		}
		catch( SQLException e )
		{
			throw new BGException( e );
		}

		return processUserTypedForward( con, mapping, form, response, FORWARD_DEFAULT );
	}

	public ActionForward getUsers( ActionMapping mapping,
								   DynActionForm form,
								   HttpServletRequest request,
								   HttpServletResponse response,
								   Connection con )
	throws BGException, SQLException
	{
		int phoneParamId = form.getParamInt( "phoneParamId", -1 );
		int fileParamId = form.getParamInt( "fileParamId", -1 );

		Config config = setup.getConfig( Config.class );
		Distribution dist = config.distrMap.get( form.getParamInt( "distrId", 0 ) );

		UserDAO userDAO = new UserDAO( con );

		List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();

		for( User user : userDAO.getUserList( dist.userGroupIds ) )
		{
			Map<String, Object> userMap = new HashMap<String, Object>();

			userMap.put( "id", String.valueOf( user.getId() ) );
			userMap.put( "title", user.getTitle() );

			ParamValueDAO paramValueDAO = new ParamValueDAO( con );
			ParameterPhoneValue parameterPhoneValue = paramValueDAO.getParamPhone( user.getId(), phoneParamId );

			if( parameterPhoneValue != null )
			{
				if( parameterPhoneValue.getItemList().size() > 0 )
				{
					ParameterPhoneValueItem item = parameterPhoneValue.getItemList().get( 0 );
					String[] parts = item.getPhoneParts();

					if( !parts[1].equals( "" ) )
					{
						userMap.put( "phone", "+" + parts[0] + "(" + parts[1] + ")" + parts[2] );
					}
					else
					{
						userMap.put( "phone", "+" + parts[0] + " " + parts[2] );
					}
				}
			}

			// TODO: убрал это параметр, по прямой ссылке файл все равно недоступен, наверное можно убрать
			//ParameterFileValue paramFileValue = paramValueDAO.getParamFile( user.getId(), fileParamId );
			//userMap.put( "fileUrl", paramFileValue.getUrl() != null ? paramFileValue.getUrl() : "" );

			AddressDistributionDAO addressDistributionDAO = new AddressDistributionDAO( con );
			List<IdTitle> userHouses = addressDistributionDAO.getUserHouses( user.getId(), null, null, null );

			List<Integer> houseIds = new ArrayList<Integer>();
			for( IdTitle idTitle : userHouses )
				houseIds.add( idTitle.getId() );

			userMap.put( "houseIds", houseIds );

			users.add( userMap );
		}

		form.getResponse().setData( "users", users );

		return processUserTypedForward( con, mapping, form, response, "" );
	}

	@Override
	protected ActionForward unspecified( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, Connection con )
	throws Exception
	{
		Config config = setup.getConfig( Config.class );

		return super.unspecified( mapping, form, request, response, con );
	}

	public ActionForward quarterDistribution( ActionMapping mapping,
											  DynActionForm form,
											  HttpServletRequest request,
											  HttpServletResponse response,
											  Connection con )
	throws Exception
	{
		Map<Integer, String> distributionMap = new HashMap<Integer, String>();

		for( Entry<Integer, ParameterMap> entry : setup.subIndexed( "quarterDistribution." ).entrySet() )
		{
			if( entry.getValue().containsKey( "title" ) )
			{
				distributionMap.put( entry.getKey(), entry.getValue().get( "title", "" ) );
			}
		}

		form.getResponse().setData( "distributionMap", distributionMap );

		return processUserTypedForward( con, mapping, form, response, "quarterDistribution" );
	}

	public ActionForward groupList( ActionMapping mapping,
									DynActionForm form,
									HttpServletRequest request,
									HttpServletResponse response,
									Connection con )
	throws BGException
	{
		int distributionId = form.getParamInt( "distrId", -1 );
		List<Group> groupList = new ArrayList<Group>();

		if( !setup.subIndexed( "quarterDistribution." ).containsKey( distributionId ) )
		{
			throw new BGMessageException( "Для выбранного распределения не заданы параметры конфигурации" );
		}

		ParameterMap config = setup.subIndexed( "quarterDistribution." ).get( distributionId );
		Set<Integer> groupIds = Utils.toIntegerSet( config.get( "userGroupIds", "" ) );
		boolean subGroups = config.getBoolean( "includeSubGroups", false );

		if( groupIds.size() == 0 )
		{
			throw new BGMessageException( "Для выбранного распределения не задан параметр 'Группы'" );
		}

		for( Integer groupId : groupIds )
		{
			Group group = UserCache.getUserGroup( groupId );

			if( group == null )
			{
				continue;
			}

			if( subGroups )
			{
				groupList.addAll( group.getChildGroupSet() );
			}
			else
			{
				groupList.add( group );
			}
		}

		Collections.sort( groupList, new GroupComparator() );
		form.getResponse().setData( "groupList", groupList );

		return processUserTypedForward( con, mapping, form, response, "groupList" );
	}

	public ActionForward undistributedGroups( ActionMapping mapping,
											  DynActionForm form,
											  HttpServletRequest request,
											  HttpServletResponse response,
											  Connection con )
	throws BGException
	{
		int distributionId = form.getParamInt( "distrId", -1 );

		if( !setup.subIndexed( "quarterDistribution." ).containsKey( distributionId ) )
		{
			throw new BGMessageException( "Для выбранного распределения не заданы параметры конфигурации" );
		}

		ParameterMap config = setup.subIndexed( "quarterDistribution." ).get( distributionId );
		List<Integer> cityIds = Utils.toIntegerList( config.get( "cityId" ) );

		if( cityIds.size() == 0 )
		{
			throw new BGMessageException( "Для выбранного распределения не задан параметр 'Город'" );
		}

		AddressDistributionDAO addressDistributionDAO = new AddressDistributionDAO( con );
		List<IdTitle> undistGroups = addressDistributionDAO.getUndistributedGroups( distributionId, cityIds );

		form.getResponse().setData( "undistGroups", undistGroups );

		return processUserTypedForward( con, mapping, form, response, "undistGroups" );
	}

	public ActionForward attachQuarterToGroup( ActionMapping mapping,
											   DynActionForm form,
											   HttpServletRequest request,
											   HttpServletResponse response,
											   Connection con )
	throws BGException
	{
		int groupId = form.getParamInt( "groupId", -1 );
		int distrId = form.getParamInt( "distrId", -1 );
		Set<Integer> quarterIds = form.getSelectedValues( "quarter" );

		if( groupId == -1 )
		{
			throw new BGMessageException( "Не выбрана группа" );
		}

		if( distrId == -1 )
		{
			throw new BGMessageException( "Не выбрано распределение" );
		}

		if( quarterIds.size() == 0 )
		{
			throw new BGMessageException( "Не выбран квартал" );
		}

		new AddressDistributionDAO( con ).addQuarterToGroup( quarterIds, distrId, groupId );

		return processJsonForward( con, form, response );
	}

	public ActionForward groupQuarters( ActionMapping mapping,
										DynActionForm form,
										HttpServletRequest request,
										HttpServletResponse response,
										Connection con )
	throws BGException
	{
		int groupId = form.getParamInt( "groupId", -1 );
		int distrId = form.getParamInt( "distrId", -1 );

		if( groupId == -1 )
		{
			throw new BGMessageException( "Не выбрана группа" );
		}

		if( distrId == -1 )
		{
			throw new BGMessageException( "Не выбрано распределение" );
		}

		List<IdTitle> groupQuarters = new AddressDistributionDAO( con ).getGroupQuarters( distrId, groupId );

		form.getResponse().setData( "groupQuarters", groupQuarters );

		return processUserTypedForward( con, mapping, form, response, "groupQuarters" );
	}

	public ActionForward removeQuarterFromGroup( ActionMapping mapping,
												 DynActionForm form,
												 HttpServletRequest request,
												 HttpServletResponse response,
												 Connection con )
	throws BGException
	{
		int groupId = form.getParamInt( "groupId", -1 );
		int distrId = form.getParamInt( "distrId", -1 );
		Set<Integer> quarterIds = form.getSelectedValues( "quarter" );

		if( groupId == -1 )
		{
			throw new BGMessageException( "Не выбрана группа" );
		}

		if( distrId == -1 )
		{
			throw new BGMessageException( "Не выбрано распределение" );
		}

		if( quarterIds.size() == 0 )
		{
			throw new BGMessageException( "Не выбран квартал" );
		}

		new AddressDistributionDAO( con ).removeQuarters( distrId, groupId, quarterIds );

		return processJsonForward( con, form, response );
	}

	//	public ActionForward getHouse( ActionMapping mapping,
	//											   DynActionForm form,
	//											   HttpServletRequest request,
	//											   HttpServletResponse response,
	//											   Connection con )
	//	throws BGException
	//	{
	//		try
	//		{
	//			AddressDistributionDAO addressDistributionDAO = new AddressDistributionDAO( con );
	//
	//			int getHouse = addressDistributionDAO.getHouseCount( form.getParamInt( "userId" ));
	//
	//			form.getResponse().setData( "house", getHouse );
	//		}
	//		catch( BGException e )
	//		{
	//			throw new BGException();
	//		}
	//
	//		return processUserTypedForward( con, mapping, form, response, "house" );
	//	}

	private class GroupComparator
	implements Comparator<Group>
	{
		@Override
		public int compare( Group group1, Group group2 )
		{
			return group1.getTitle().compareTo( group2.getTitle() );
		}
	}
}

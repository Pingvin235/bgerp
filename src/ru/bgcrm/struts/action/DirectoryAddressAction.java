package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.AnalyticDAO;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.analytic.HouseCapacityItem;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressCountry;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class DirectoryAddressAction extends BaseAction {
    public DirectoryAddressAction() {
        super();
    }

    public ActionForward address(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ParameterMap permission = form.getPermission();
        AddressDAO addressDAO = new AddressDAO(con);

        String searchMode = form.getParam("searchMode");
        String selectTab = form.getParam("selectTab");
        if (Utils.isBlankString(selectTab)) {
            form.setParam("selectTab", selectTab = "street");
        }

        int addressCountryId = Utils.parseInt(form.getParam("addressCountryId"));
        int addressCityId = Utils.parseInt(form.getParam("addressCityId"));
        int addressStreetId = Utils.parseInt(form.getParam("addressItemId"));

        Set<Integer> allowedCityIds = Utils.toIntegerSet(permission.get("cityIds"));

        if (addressStreetId > 0) {
            //searchMode = "house";
            AddressItem addressStreet = addressDAO.getAddressStreet(addressStreetId, true, true);
            if (addressStreet != null) {
                AddressCity addressCity = addressStreet.getAddressCity();
                AddressCountry addressCountry = addressCity.getAddressCountry();
                form.setParam("addressItemTitle", addressStreet.getTitle());
                form.setParam("addressItemId", String.valueOf(addressStreet.getId()));
                form.setParam("addressCityTitle", addressCity.getTitle());
                form.setParam("addressCityId", String.valueOf(addressCity.getId()));
                form.setParam("addressCountryTitle", addressCountry.getTitle());
                form.setParam("addressCountryId", String.valueOf(addressCountry.getId()));
            }
        } else if (addressCityId > 0) {
            //searchMode = "item";
            AddressCity addressCity = addressDAO.getAddressCity(addressCityId, true);
            if (addressCity != null) {
                AddressCountry addressCountry = addressCity.getAddressCountry();
                form.setParam("addressCityTitle", addressCity.getTitle());
                form.setParam("addressCityId", String.valueOf(addressCity.getId()));
                form.setParam("addressCountryTitle", addressCountry.getTitle());
                form.setParam("addressCountryId", String.valueOf(addressCountry.getId()));
            }
        } else if (addressCountryId > 0) {
            //searchMode = "city";
            AddressCountry addressCountry = addressDAO.getAddressCountry(addressCountryId);
            if (addressCountry != null) {
                form.setParam("addressCountryTitle", addressCountry.getTitle());
                form.setParam("addressCountryId", String.valueOf(addressCountry.getId()));
            }
        }

        if (searchMode == null) {
            searchMode = "country";
        }

        form.setParam("searchMode", searchMode);

        if ("house".equals(searchMode)) {
            String addressHouse = form.getParam("addressHouse");
            SearchResult<AddressHouse> searchResult = new SearchResult<AddressHouse>(form);
            addressDAO.searchAddressHouseList(searchResult, addressStreetId, addressHouse, true, true, true, true);
        } else if ("item".equals(searchMode)) {
            if ("area".equals(selectTab)) {
                String addressItemTitle = form.getParam("addressItemTitle");
                SearchResult<AddressItem> searchResult = new SearchResult<AddressItem>(form);
                addressDAO.searchAddressAreaList(searchResult, addressCityId, CommonDAO.getLikePattern(addressItemTitle, "subs"), true, true);
            } else if ("quarter".equals(selectTab)) {
                String addressItemTitle = form.getParam("addressItemTitle");
                SearchResult<AddressItem> searchResult = new SearchResult<AddressItem>(form);
                addressDAO.searchAddressQuarterList(searchResult, addressCityId, CommonDAO.getLikePattern(addressItemTitle, "subs"), true, true);
            } else if ("street".equals(selectTab)) {
                String addressItemTitle = form.getParam("addressItemTitle");
                SearchResult<AddressItem> searchResult = new SearchResult<AddressItem>(form);
                addressDAO.searchAddressStreetList(searchResult, Collections.singleton(addressCityId),
                        CommonDAO.getLikePattern(addressItemTitle, "subs"), true, true);
            }
        } else if ("city".equals(searchMode)) {
            String addressCityTitle = form.getParam("addressCityTitle");
            SearchResult<AddressCity> searchResult = new SearchResult<AddressCity>(form);
            addressDAO.searchAddressCityList(searchResult, addressCountryId, CommonDAO.getLikePattern(addressCityTitle, "subs"), true,
                    allowedCityIds);
        } else if ("country".equals(searchMode)) {
            String addressCountryTitle = form.getParam("addressCountryTitle");
            SearchResult<AddressCountry> searchResult = new SearchResult<AddressCountry>(form);
            addressDAO.searchAddressCountryList(searchResult, CommonDAO.getLikePattern(addressCountryTitle, "subs"));
        }

        return data(con, mapping, form, FORWARD_DEFAULT);
    }

    public ActionForward addressGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        AddressDAO addressDAO = new AddressDAO(con);
        AnalyticDAO analyticDAO = new AnalyticDAO(con);

        int addressHouseId = Utils.parseInt(form.getParam("addressHouseId"), -1);
        int addressStreetId = Utils.parseInt(form.getParam("addressItemId"), -1);
        int addressCityId = Utils.parseInt(form.getParam("addressCityId"), -1);
        int addressCountryId = Utils.parseInt(form.getParam("addressCountryId"), -1);

        if (addressHouseId >= 0) {
            AddressHouse addressHouse = addressDAO.getAddressHouse(addressHouseId, true, true, true);
            if (addressHouse == null) {
                addressHouse = new AddressHouse();
                addressHouse.setStreetId(addressStreetId);
                addressHouse.setAddressStreet(addressDAO.getAddressStreet(addressStreetId, true, true));
            }

            // ёмкость домов
            List<HouseCapacityItem> houseCapacityItems = analyticDAO.getHouseCapacityItemList(addressHouse.getId());
            StringBuffer buf = new StringBuffer();
            for (HouseCapacityItem item : houseCapacityItems) {
                buf.append(item.getServiceType());
                buf.append("\t");
                buf.append(TimeUtils.format(item.getDate(), TimeUtils.FORMAT_TYPE_YMD));
                buf.append("\t");
                buf.append(item.getValue());
                buf.append("\n");
            }

            if (Utils.isBlankString(form.getParam("addressCountryTitle"))) {
                form.setParam("addressCountryTitle", addressHouse.getAddressStreet().getAddressCity().getAddressCountry().getTitle());
                form.setParam("addressCityTitle", addressHouse.getAddressStreet().getAddressCity().getTitle());
                //form.setParam( "addressItemTitle", addressHouse.getAddressStreet().getTitle() );
            }

            form.getResponse().setData("house", addressHouse);
            form.getResponse().setData("capacity", houseCapacityItems);

            form.setParam("capacity", buf.toString());
            form.setParam("config", getConfigString(addressHouse.getConfig()));

            final int cityId = addressHouse.getAddressStreet().getCityId();
            HttpServletRequest request = form.getHttpRequest();

            SearchResult<AddressItem> searchResultStreet = new SearchResult<AddressItem>();
            addressDAO.searchAddressStreetList(searchResultStreet, cityId);
            request.setAttribute("parameterAddressStreetList", searchResultStreet.getList());

            SearchResult<AddressItem> searchResultArea = new SearchResult<AddressItem>();
            addressDAO.searchAddressAreaList(searchResultArea, cityId);
            request.setAttribute("parameterAddressAreaList", searchResultArea.getList());

            SearchResult<AddressItem> searchResultQuarter = new SearchResult<AddressItem>();
            addressDAO.searchAddressQuarterList(searchResultQuarter, cityId);
            request.setAttribute("parameterAddressQuarterList", searchResultQuarter.getList());
        } else if (getAddressItem(form, con)) {
        } else if (addressCityId >= 0) {
            AddressCity addressCity = addressDAO.getAddressCity(addressCityId, true);
            if (addressCity == null) {
                addressCity = new AddressCity();

                addressCity.setCountryId(addressCountryId);
                addressCity.setAddressCountry(addressDAO.getAddressCountry(addressCountryId));
            }

            form.setParam("title", addressCity.getTitle());
            form.setParam("config", getConfigString(addressCity.getConfig()));

            // по факту пока не используется
            form.getResponse().setData("city", addressCity);
        } else if (addressCountryId >= 0) {
            AddressCountry addressCountry = addressDAO.getAddressCountry(addressCountryId);
            if (addressCountry == null) {
                addressCountry = new AddressCountry();
            }

            form.setParam("title", addressCountry.getTitle());
            form.setParam("config", getConfigString(addressCountry.getConfig()));

            // по факту пока не используется
            form.getResponse().setData("country", addressCountry);
        }

        return data(con, mapping, form, FORWARD_DEFAULT);
    }

    private boolean getAddressItem(DynActionForm form, Connection con) throws Exception {
        AddressDAO addressDAO = new AddressDAO(con);

        int addressItemId = Utils.parseInt(form.getParam("addressItemId"), -1);
        int addressCityId = Utils.parseInt(form.getParam("addressCityId"), -1);
        String itemType = form.getParam("selectTab");

        if (addressItemId >= 0) {
            AddressItem addressItem = null;
            if (itemType != null) {
                if ("street".equals(itemType)) {
                    addressItem = addressDAO.getAddressStreet(addressItemId, true, true);
                } else if ("area".equals(itemType)) {
                    addressItem = addressDAO.getAddressArea(addressItemId, true, true);
                } else if ("quarter".equals(itemType)) {
                    addressItem = addressDAO.getAddressQuarter(addressItemId, true, true);
                }
            }

            if (addressItem == null) {
                addressItem = new AddressItem();

                addressItem.setCityId(addressCityId);
                addressItem.setAddressCity(addressDAO.getAddressCity(addressCityId, true));
            }

            form.setParam("title", addressItem.getTitle());
            form.setParam("config", getConfigString(addressItem.getConfig()));

            form.getResponse().setData(itemType.toLowerCase(), addressItem);

            return true;
        }
        return false;
    }

    public ActionForward addressUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        AddressDAO addressDAO = new AddressDAO(con);
        AnalyticDAO analyticDAO = new AnalyticDAO(con);

        int addressHouseId = Utils.parseInt(form.getParam("addressHouseId"), -1);
        int addressStreetId = Utils.parseInt(form.getParam("addressItemId"), -1);
        int addressCityId = Utils.parseInt(form.getParam("addressCityId"), -1);
        int addressCountryId = Utils.parseInt(form.getParam("addressCountryId"), -1);

        ParameterMap permission = form.getPermission();
        Set<Integer> allowedCityIds = Utils.toIntegerSet(permission.get("cityIds"));
        boolean restrictUpdateMainParameters = Utils.parseBoolean(permission.get("restrictUpdateMainParameters"));

        if (addressHouseId >= 0) {
            AddressHouse addressHouse = new AddressHouse();
            addressHouse.setId(addressHouseId);
            addressHouse.setStreetId(addressStreetId);
            addressHouse.setAddressStreet(addressDAO.getAddressStreet(addressStreetId, true, true));

            //TODO: Переписать на единую систему параметров!!!
            String boxIndexKey = setup.get("directory.address.config.post_index.key", "s.box.index");
            String flatAmountKey = setup.get("directory.address.config.flat_amount.key", ".i.flat.amount");
            String commentInternetKey = setup.get("directory.address.config.comment_internet.key", "billing.service.type.internet");
            String commentKtvKey = setup.get("directory.address.config.comment_ktv.key", "billing.service.type.tv");
            String routeKey = setup.get("directory.address.config.route.key", "s.route");

            checkCityAllow(allowedCityIds, addressHouse.getAddressStreet().getCityId());

            addressHouse.setAreaId(Utils.parseInt(form.getParam("addressAreaId")));
            addressHouse.setQuarterId(Utils.parseInt(form.getParam("addressQuarterId")));
            addressHouse.setStreetId(addressStreetId);
            addressHouse.setHouseAndFrac(form.getParam("house"));

            if (!checkUpdatePermissions(restrictUpdateMainParameters, addressHouse, addressDAO.getAddressHouse(addressHouseId, true, true, true))) {
                throw new BGMessageException("Редактирование основных параметров запрещено!");
            }

            addressHouse.setComment(form.getParam("comment", ""));
            addressHouse.setConfig(form.getParam("config", ""));

            String postIndex = form.getParam("postIndex", "");
            addressHouse.setPostIndex(postIndex);

            // оставлено для совместимости на уровне БД, может кто-то ещё берёт из address_config индексы
            if (postIndex != null && !postIndex.isEmpty()) {
                addressHouse.getConfig().put(boxIndexKey, postIndex);
            }
            String flatAmount = form.getParam("flatAmount");
            if (flatAmount != null && !flatAmount.isEmpty()) {
                addressHouse.getConfig().put(flatAmountKey, String.valueOf(Utils.parseInt(flatAmount, 0)));
            }
            String commentInternet = form.getParam("commentInternet");
            if (commentInternet != null && !commentInternet.isEmpty()) {
                addressHouse.getConfig().put(commentInternetKey, commentInternet.trim());
            }
            String commentKtv = form.getParam("commentKtv");
            if (commentKtv != null && !commentKtv.isEmpty()) {
                addressHouse.getConfig().put(commentKtvKey, commentKtv.trim());
            }
            String route = form.getParam("route");
            if (route != null && !route.isEmpty()) {
                addressHouse.getConfig().put(routeKey, route.trim());
            }
            addressDAO.updateAddressHouse(addressHouse);
            //
            List<HouseCapacityItem> houseCapacityItems = new ArrayList<HouseCapacityItem>();
            analyticDAO.deleteHouseCapacityItem(addressHouse.getId());
            String houseCapacity = form.getParam("capacity", "");
            for (String item : houseCapacity.split("\n")) {
                String[] tokens = item.trim().split(" |\t");
                if (tokens.length == 3) {
                    HouseCapacityItem capacityItem = new HouseCapacityItem();
                    capacityItem.setHouseId(addressHouse.getId());
                    capacityItem.setServiceType(tokens[0]);
                    capacityItem.setDate(TimeUtils.parse(tokens[1], TimeUtils.FORMAT_TYPE_YMD));
                    capacityItem.setValue(Utils.parseInt(tokens[2], -1));
                    if (capacityItem.getValue() > 0) {
                        houseCapacityItems.add(capacityItem);
                    }
                }
            }
            if (houseCapacityItems.size() > 0) {
                for (HouseCapacityItem houseCapacityItem : houseCapacityItems) {
                    analyticDAO.updateHouseCapacityItem(houseCapacityItem);
                }
            }

            new ParamValueDAO(con).updateParamsAddressOnHouseUpdate(addressHouseId);
        } else if (updateAddressItem(form, con)) {
        } else if (addressCityId >= 0) {
            AddressCity addressCity = new AddressCity();
            addressCity.setId(addressCityId);
            addressCity.setCountryId(addressCountryId);
            addressCity.setAddressCountry(addressDAO.getAddressCountry(addressCountryId));

            checkCityRestriction(allowedCityIds);

            addressCity.setTitle(form.getParam("title"));
            addressDAO.updateAddressCity(addressCity);
        } else if (addressCountryId >= 0) {
            AddressCountry addressCountry = new AddressCountry();
            addressCountry.setId(addressCountryId);

            checkCityRestriction(allowedCityIds);

            addressCountry.setTitle(form.getParam("title"));
            addressDAO.updateAddressCountry(addressCountry);
        }

        return status(con, form);
    }

    private boolean checkUpdatePermissions(boolean restrictUpdateMainParameters, AddressHouse newAddressHouse, AddressHouse oldAddressHouse) {
        if (!restrictUpdateMainParameters || oldAddressHouse == null) {
            return true;
        }

        if (newAddressHouse.getStreetId() != oldAddressHouse.getStreetId() || newAddressHouse.getAreaId() != oldAddressHouse.getAreaId()
                || newAddressHouse.getQuarterId() != oldAddressHouse.getQuarterId()
                || !newAddressHouse.getHouseAndFrac().equals(oldAddressHouse.getHouseAndFrac())) {
            return false;
        }

        return true;
    }

    private boolean updateAddressItem(DynActionForm form, Connection con) throws Exception {
        AddressDAO addressDAO = new AddressDAO(con);

        int addressCityId = Utils.parseInt(form.getParam("addressCityId"), -1);
        int addressItemId = Utils.parseInt(form.getParam("addressItemId"), -1);
        String itemType = form.getParam("selectTab");

        if (addressItemId >= 0) {
            ParameterMap permission = form.getPermission();
            Set<Integer> allowedCityIds = Utils.toIntegerSet(permission.get("cityIds"));

            checkCityAllow(allowedCityIds, addressCityId);

            AddressItem addressItem = new AddressItem();
            addressItem.setId(addressItemId);
            addressItem.setCityId(addressCityId);
            addressItem.setAddressCity(addressDAO.getAddressCity(addressCityId, true));

            addressItem.setTitle(form.getParam("title"));

            if (itemType != null) {
                if ("street".equals(itemType)) {
                    addressDAO.updateAddressStreet(addressItem);
                } else if ("area".equals(itemType)) {
                    addressDAO.updateAddressArea(addressItem);
                } else if ("quarter".equals(itemType)) {
                    addressDAO.updateAddressQuarter(addressItem);
                }
            }
            return true;
        }
        return false;
    }

    public ActionForward addressDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ParameterMap permission = form.getPermission();
        Set<Integer> allowedCityIds = Utils.toIntegerSet(permission.get("cityIds"));

        AddressDAO addressDAO = new AddressDAO(con);

        int addressHouseId = Utils.parseInt(form.getParam("addressHouseId"), -1);
        int addressCityId = Utils.parseInt(form.getParam("addressCityId"), -1);
        int addressCountryId = Utils.parseInt(form.getParam("addressCountryId"), -1);

        if (addressHouseId >= 0) {
            AddressHouse addressHouse = addressDAO.getAddressHouse(addressHouseId, true, true, true);

            checkCityAllow(allowedCityIds, addressHouse.getAddressStreet().getCityId());

            addressDAO.deleteAddressHouse(addressHouseId);
        } else if (deleteAddressItem(form, con)) {
        } else if (addressCityId >= 0) {
            AddressCity addressCity = new AddressCity();
            addressCity.setId(addressCityId);
            addressCity.setCountryId(addressCountryId);
            addressCity.setAddressCountry(addressDAO.getAddressCountry(addressCountryId));

            checkCityRestriction(allowedCityIds);
            addressDAO.deleteAddressCity(addressCityId);
        } else if (addressCountryId >= 0) {
            AddressCountry addressCountry = new AddressCountry();
            addressCountry.setId(addressCountryId);

            checkCityRestriction(allowedCityIds);
            addressDAO.deleteAddressCountry(addressCountryId);
        }

        return status(con, form);
    }

    private boolean deleteAddressItem(DynActionForm form, Connection con) throws Exception {
        AddressDAO addressDAO = new AddressDAO(con);

        int addressItemId = Utils.parseInt(form.getParam("addressItemId"), -1);
        String itemType = form.getParam("selectTab");

        if (addressItemId >= 0) {
            ParameterMap permission = form.getPermission();
            Set<Integer> allowedCityIds = Utils.toIntegerSet(permission.get("cityIds"));

            if ("street".equals(itemType)) {
                AddressItem item = addressDAO.getAddressStreet(addressItemId, false, true);

                checkCityAllow(allowedCityIds, item.getCityId());

                addressDAO.deleteAddressStreet(addressItemId);
            } else if ("area".equals(itemType)) {
                AddressItem item = addressDAO.getAddressArea(addressItemId, false, true);

                checkCityAllow(allowedCityIds, item.getCityId());

                addressDAO.deleteAddressArea(addressItemId);
            } else if ("quarter".equals(itemType)) {
                AddressItem item = addressDAO.getAddressQuarter(addressItemId, false, true);

                checkCityAllow(allowedCityIds, item.getCityId());

                addressDAO.deleteAddressQuarter(addressItemId);
            }
            return true;

        }
        return false;
    }

    private void checkCityRestriction(Set<Integer> allowedCityIds) throws BGMessageException {
        if (allowedCityIds.size() != 0) {
            throw new BGMessageException("Установлено ограничение по городам.");
        }
    }

    private void checkCityAllow(Set<Integer> allowedCityIds, int cityId) throws BGMessageException {
        if (allowedCityIds.size() != 0 && !allowedCityIds.contains(cityId)) {
            throw new BGMessageException("Запрещено для данного города.");
        }
    }

    protected String getConfigString(Map<String, String> configMap) {
        StringBuilder config = new StringBuilder();
        if (configMap != null) {
            for (String key : configMap.keySet()) {
                config.append(key);
                config.append("=");
                config.append(configMap.get(key));
                config.append("\n");
            }
        }
        return config.toString();
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        return address(mapping, form, con);
    }

    // поиск для редакторов по подстроке
    public ActionForward streetSearch(ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response,
            Connection con) throws Exception {
        AddressDAO addressDAO = new AddressDAO(con);

        String title = form.getParam("title", "");

        //TODO: передалать механизм передачи cityIds в DAO для случае если разрешённых городов нет
        Set<Integer> cityIds = Utils.toIntegerSet(form.getPermission().get("cityIds"));

        SearchResult<AddressItem> searchResult = new SearchResult<AddressItem>(form);
        addressDAO.searchAddressStreetList(searchResult, cityIds, CommonDAO.getLikePattern(title, "subs"), true, true);

        return data(con, mapping, form, "???");
    }

    public ActionForward houseSearch(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        AddressDAO addressDAO = new AddressDAO(con);

        int streetId = Utils.parseInt(form.getParam("streetId"));
        String house = form.getParam("house", "");

        SearchResult<AddressHouse> searchResult = new SearchResult<AddressHouse>(form);
        addressDAO.searchAddressHouseList(searchResult, streetId, house);

        return data(con, mapping, form, "???");
    }
}

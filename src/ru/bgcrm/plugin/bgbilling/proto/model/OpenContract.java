package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.LinkedList;
import java.util.List;

import org.bgerp.model.base.IdTitle;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.util.Utils;

/*
 * Класс для структурного хранения результата выполнения действия биллинга OpenContract
 */
public class OpenContract
{
    private List<IdTitle> filters = null; // Группы договоров
    private List<IdTitle> cities = null;
    private List<IdTitle> streets = null;
    private List<IdTitle> parametersText = null;
    private List<IdTitle> parametersList = null;
    private List<IdTitle> parametersPhone = null;
    private List<IdTitle> parametersAddress = null;
    private List<IdTitle> parametersDate = null;
    private List<IdTitle> parametersContractService = null;
    private List<IdTitle> parametersEMail = null;
    private List<IdTitle> parametersFlag = null;
    private List<IdTitle> parametersContract = null;
    private List<IdTitle> parametersObjectText = null;
    private List<IdTitle> parametersObjectAddress = null;
    private List<IdTitle> parametersObjectList = null;
    private List<IdTitle> parametersObjectFlag = null;
    private List<IdTitle> parametersObjectDate = null;

    public List<IdTitle> getFilters()
    {
        return filters;
    }

    public void setFilters( List<IdTitle> filters )
    {
        this.filters = filters;
    }

    public List<IdTitle> getCities()
    {
        return cities;
    }

    public void setCities( List<IdTitle> cities )
    {
        this.cities = cities;
    }

    public List<IdTitle> getStreets()
    {
        return streets;
    }

    public void setStreets( List<IdTitle> streets )
    {
        this.streets = streets;
    }

    public List<IdTitle> getParametersText()
    {
        return parametersText;
    }

    public void setParametersText( List<IdTitle> parametersText )
    {
        this.parametersText = parametersText;
    }

    public List<IdTitle> getParametersList()
    {
        return parametersList;
    }

    public void setParametersList( List<IdTitle> parametersList )
    {
        this.parametersList = parametersList;
    }

    public List<IdTitle> getParametersPhone()
    {
        return parametersPhone;
    }

    public void setParametersPhone( List<IdTitle> parametersPhone )
    {
        this.parametersPhone = parametersPhone;
    }

    public List<IdTitle> getParametersAddress()
    {
        return parametersAddress;
    }

    public void setParametersAddress( List<IdTitle> parametersAddress )
    {
        this.parametersAddress = parametersAddress;
    }

    public List<IdTitle> getParametersDate()
    {
        return parametersDate;
    }

    public void setParametersDate( List<IdTitle> parametersDate )
    {
        this.parametersDate = parametersDate;
    }

    public List<IdTitle> getParametersContractService()
    {
        return parametersContractService;
    }

    public void setParametersContractService( List<IdTitle> parametersContractService )
    {
        this.parametersContractService = parametersContractService;
    }

    public List<IdTitle> getParametersEMail()
    {
        return parametersEMail;
    }

    public void setParametersEMail( List<IdTitle> parametersEMail )
    {
        this.parametersEMail = parametersEMail;
    }

    public List<IdTitle> getParametersFlag()
    {
        return parametersFlag;
    }

    public void setParametersFlag( List<IdTitle> parametersFlag )
    {
        this.parametersFlag = parametersFlag;
    }

    public List<IdTitle> getParametersContract()
    {
        return parametersContract;
    }

    public void setParametersContract( List<IdTitle> parametersContract )
    {
        this.parametersContract = parametersContract;
    }

    public List<IdTitle> getParametersObjectText()
    {
        return parametersObjectText;
    }

    public void setParametersObjectText( List<IdTitle> parametersObjectText )
    {
        this.parametersObjectText = parametersObjectText;
    }

    public List<IdTitle> getParametersObjectAddress()
    {
        return parametersObjectAddress;
    }

    public void setParametersObjectAddress( List<IdTitle> parametersObjectAddress )
    {
        this.parametersObjectAddress = parametersObjectAddress;
    }

    public List<IdTitle> getParametersObjectList()
    {
        return parametersObjectList;
    }

    public void setParametersObjectList( List<IdTitle> parametersObjectList )
    {
        this.parametersObjectList = parametersObjectList;
    }

    public List<IdTitle> getParametersObjectFlag()
    {
        return parametersObjectFlag;
    }

    public void setParametersObjectFlag( List<IdTitle> parametersObjectFlag )
    {
        this.parametersObjectFlag = parametersObjectFlag;
    }

    public List<IdTitle> getParametersObjectDate()
    {
        return parametersObjectDate;
    }

    public void setParametersObjectDate( List<IdTitle> parametersObjectDate )
    {
        this.parametersObjectDate = parametersObjectDate;
    }

    public OpenContract( Document document )
    {
        setFilters(new LinkedList<>() );
        for( Element filter : XMLUtils.selectElements( document,
                                                       "/data/filters/item" ) )
        {
            getFilters().add( new IdTitle( Utils.parseInt( filter.getAttribute( "id" ) ),
                                           filter.getAttribute( "title" ) ) );
        }

        setCities(new LinkedList<>() );
        for( Element city : XMLUtils.selectElements( document,
                                                     "/data/cities/item" ) )
        {
            getCities().add( new IdTitle( Utils.parseInt( city.getAttribute( "id" ) ),
                                          city.getAttribute( "title" ) ) );
        }

        setStreets(new LinkedList<>() );
        for( Element street : XMLUtils.selectElements( document,
                                                       "/data/streets/item" ) )
        {
            getStreets().add( new IdTitle( Utils.parseInt( street.getAttribute( "id" ) ),
                                           street.getAttribute( "title" ) ) );
        }

        // TODO: Инициализацию остальных полей добавить по мере необходимости
    }
}

package com.kltn.mapper;

import com.kltn.dto.entity.CriteriaDto;
import com.kltn.dto.request.criteria.CreateCriteriaRequest;
import com.kltn.model.Criteria;
import com.kltn.model.Size;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = { SizeMapper.class })
public interface CriteriaMapper {
    CriteriaDto toCriteriaDto(Criteria criteria);

    @Mapping(target = "id", ignore = true)

    Criteria toCriteria(CreateCriteriaRequest criteria);


    Criteria toCriteria(CriteriaDto criteria);

    @Named("mapSize")
    default Size mapSize(Long idSize) {
        if (idSize == null) {
            return null;
        }
        Size size = new Size();
        size.setId(idSize);
        return size;
    }
}

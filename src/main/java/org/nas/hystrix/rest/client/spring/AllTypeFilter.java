package org.nas.hystrix.rest.client.spring;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
public class AllTypeFilter implements TypeFilter {

    private final List<TypeFilter> delegates;

    /**
     * Creates a new {@link AllTypeFilter} to match if all the given delegates match.
     *
     * @param delegates must not be {@literal null}.
     */
    public AllTypeFilter(List<TypeFilter> delegates) {
        Objects.nonNull(delegates);
        this.delegates = delegates;
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {

        for (TypeFilter filter : this.delegates) {
            if (!filter.match(metadataReader, metadataReaderFactory)) {
                return false;
            }
        }

        return true;
    }

}

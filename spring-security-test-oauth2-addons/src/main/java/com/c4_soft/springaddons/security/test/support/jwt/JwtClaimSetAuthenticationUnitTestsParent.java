/*
 * Copyright 2019 Jérôme Wacongne
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.c4_soft.springaddons.security.test.support.jwt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.junit4.SpringRunner;

import com.c4_soft.oauth2.rfc7519.JwtClaimSet;
import com.c4_soft.springaddons.security.test.support.Defaults;

/**
 * @author Ch4mp
 *
 */
@RunWith(SpringRunner.class)
@Import(JwtClaimSetAuthenticationUnitTestsParent.UnitTestConfig.class)
public abstract class JwtClaimSetAuthenticationUnitTestsParent {

	@Autowired
	BeanFactory beanFactory;

	public JwtClaimSetAuthenticationRequestPostProcessor securityRequestPostProcessor() {
		return beanFactory.getBean(JwtClaimSetAuthenticationRequestPostProcessor.class);
	}

	public JwtClaimSetAuthenticationRequestPostProcessor securityRequestPostProcessor(Consumer<JwtClaimSet.Builder<?>> claimsConsumer) {
		final var requestPostProcessor = securityRequestPostProcessor();
		requestPostProcessor.claims(claimsConsumer);
		return requestPostProcessor;
	}

	public JwtClaimSetAuthenticationWebTestClientConfigurer securityWebTestClientConfigurer() {
		return beanFactory.getBean(JwtClaimSetAuthenticationWebTestClientConfigurer.class);
	}

	public JwtClaimSetAuthenticationWebTestClientConfigurer securityWebTestClientConfigurer(Consumer<JwtClaimSet.Builder<?>> claimsConsumer) {
		final var webTestClientConfigurer = securityWebTestClientConfigurer();
		webTestClientConfigurer.claims(claimsConsumer);
		return webTestClientConfigurer;
	}

	@TestConfiguration
	public static class UnitTestConfig {

		@ConditionalOnMissingBean
		@Bean
		public JwtDecoder jwtDecoder() {
			return mock(JwtDecoder.class);
		}

		@ConditionalOnMissingBean
		@Bean
		@Scope("prototype")
		public Converter<JwtClaimSet, Set<GrantedAuthority>> authoritiesConverter() {
			final var mockAuthoritiesConverter = mock(JwtClaimSet2AuthoritiesConverter.class);

			when(mockAuthoritiesConverter.convert(any())).thenReturn(Defaults.GRANTED_AUTHORITIES);

			return mockAuthoritiesConverter;
		}

		@Bean
		@Scope("prototype")
		public JwtClaimSetAuthenticationWebTestClientConfigurer jwtClaimSetAuthenticationWebTestClientConfigurer(
				Converter<JwtClaimSet, Set<GrantedAuthority>> authoritiesConverter) {
			return new JwtClaimSetAuthenticationWebTestClientConfigurer(authoritiesConverter);
		}

		@Bean
		@Scope("prototype")
		public JwtClaimSetAuthenticationRequestPostProcessor jwtClaimSetAuthenticationRequestPostProcessor(
				Converter<JwtClaimSet, Set<GrantedAuthority>> authoritiesConverter) {
			return new JwtClaimSetAuthenticationRequestPostProcessor(authoritiesConverter);
		}

		private static interface JwtClaimSet2AuthoritiesConverter extends Converter<JwtClaimSet, Set<GrantedAuthority>> {
		}
	}

}

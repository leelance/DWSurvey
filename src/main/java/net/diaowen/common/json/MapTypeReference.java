package net.diaowen.common.json;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

/**
 * MapTypeReference
 *
 * @author lance
 * @since 2023/3/9 09:28
 */
class MapTypeReference extends TypeReference<Map<String, Object>> {
  MapTypeReference() {
  }
}

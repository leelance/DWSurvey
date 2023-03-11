package net.diaowen.dwsurvey.repository.answer;

import net.diaowen.dwsurvey.entity.AnUplodFile;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * answer upload file
 *
 * @author lance
 * @since 2023/3/11 00:32
 */
public interface AnUploadFileRepository extends CrudRepository<AnUplodFile, String>, JpaSpecificationExecutor<AnUplodFile> {

  /**
   * 根据quId查询答案
   *
   * @param quId 题目id
   * @return List<Object [ ]>
   */
  @Query(value = "select count(case when file_path='' then file_path end) emptyCount, count(case when file_path!='' then file_path end) blankCount from t_an_uplodfile where visibility=1 and qu_id=?1", nativeQuery = true)
  Map<String, BigInteger> findGroupStats(String quId);

  /**
   * 查询上传文件答案
   *
   * @param belongId 问卷id
   * @return List
   */
  List<AnUplodFile> findByBelongId(String belongId);

  /**
   * 查询上传文件答案
   *
   * @param belongAnswerId belongAnswerId
   * @param quId           quId
   * @return list
   */
  List<AnUplodFile> findByBelongAnswerIdAndQuId(String belongAnswerId, String quId);
}

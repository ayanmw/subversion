/**
 * @copyright
 * ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 * ====================================================================
 * @endcopyright
 *
 * @file svn_io_private.h
 * @brief Private IO API
 */

#ifndef SVN_IO_PRIVATE_H
#define SVN_IO_PRIVATE_H

#include <apr.h>

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

/* The flags to pass to apr_stat to check for executable and/or readonly */
#if defined(WIN32) || defined(__OS2__)
#define SVN__APR_FINFO_EXECUTABLE (0)
#define SVN__APR_FINFO_READONLY (0)
#define SVN__APR_FINFO_MASK_OUT (APR_FINFO_PROT | APR_FINFO_OWNER)
#else
#define SVN__APR_FINFO_EXECUTABLE (APR_FINFO_PROT)
#define SVN__APR_FINFO_READONLY (APR_FINFO_PROT | APR_FINFO_OWNER)
#define SVN__APR_FINFO_MASK_OUT (0)
#endif

/* 90% of the lines we encounter will be less than this many chars.
 *
 * Line-based functions like svn_stream_readline should fetch data in
 * blocks no longer than this.  Although using a larger prefetch size is
 * not illegal and must not break any functionality, it may be
 * significantly less efficient in certain situations.
 */
#define SVN__LINE_CHUNK_SIZE 80


/** Set @a *executable TRUE if @a file_info is executable for the
 * user, FALSE otherwise.
 *
 * Always returns FALSE on Windows or platforms without user support.
 */
svn_error_t *
svn_io__is_finfo_executable(svn_boolean_t *executable,
                            apr_finfo_t *file_info,
                            apr_pool_t *pool);

/** Set @a *read_only TRUE if @a file_info is read-only for the user,
 * FALSE otherwise.
 */
svn_error_t *
svn_io__is_finfo_read_only(svn_boolean_t *read_only,
                           apr_finfo_t *file_info,
                           apr_pool_t *pool);


/**
 * Lock file at @a lock_file. If that file does not exist, create an empty
 * file.
 *
 * Lock will be automatically released when @a pool is cleared or destroyed.
 * Use @a pool for memory allocations.
 */
svn_error_t *
svn_io__file_lock_autocreate(const char *lock_file,
                             apr_pool_t *pool);


/** Buffer test handler function for a generic stream. @see svn_stream_t
 * and svn_stream__is_buffered().
 *
 * @since New in 1.7.
 */
typedef svn_boolean_t (*svn_stream__is_buffered_fn_t)(void *baton);

/** Set @a stream's buffer test function to @a is_buffered_fn
 *
 * @since New in 1.7.
 */
void
svn_stream__set_is_buffered(svn_stream_t *stream,
                            svn_stream__is_buffered_fn_t is_buffered_fn);

/** Return whether this generic @a stream uses internal buffering.
 * This may be used to work around subtle differences between buffered
 * and non-buffered APR files.  A lazy-open stream cannot report the
 * true buffering state until after the lazy open: a stream that
 * initially reports as non-buffered may report as buffered later.
 *
 * @since New in 1.7.
 */
svn_boolean_t
svn_stream__is_buffered(svn_stream_t *stream);

/** Return the underlying file, if any, associated with the stream, or
 * NULL if not available.  Accessing the file bypasses the stream.
 */
apr_file_t *
svn_stream__aprfile(svn_stream_t *stream);

/* Creates as *INSTALL_STREAM a stream that once completed can be installed
   using Windows checkouts much slower than Unix.

   While writing the stream is temporarily stored in TMP_ABSPATH.
 */
svn_error_t *
svn_stream__create_for_install(svn_stream_t **install_stream,
                               const char *tmp_abspath,
                               apr_pool_t *result_pool,
                               apr_pool_t *scratch_pool);

/* Installs a stream created with svn_stream__create_for_install in its final
   location FINAL_ABSPATH, potentially using platform specific optimizations.

   If MAKE_PARENTS is TRUE, this function will create missing parent
   directories if needed.
 */
svn_error_t *
svn_stream__install_stream(svn_stream_t *install_stream,
                           const char *final_abspath,
                           svn_boolean_t make_parents,
                           apr_pool_t *scratch_pool);

/* Deletes the install stream (when installing is not necessary after all) */
svn_error_t *
svn_stream__install_delete(svn_stream_t *install_stream,
                           apr_pool_t *scratch_pool);

/* Optimized apr_file_stat / apr_file_info_get operating on a closed
   install stream */
svn_error_t *
svn_stream__install_get_info(apr_finfo_t *finfo,
                             svn_stream_t *install_stream,
                             apr_int32_t wanted,
                             apr_pool_t *scratch_pool);


#if defined(WIN32)

/* ### Move to something like io.h or subr.h, to avoid making it
       part of the DLL api */

/* This is semantically the same as the APR utf8_to_unicode_path
   function, but reimplemented here because APR does not export it.

   Note that this function creates "\\?\" paths so the resulting path
   can only be used for WINAPI functions that explicitly document support
   for this kind of paths. Newer Windows functions (Vista+) that support
   long paths directly DON'T want this kind of escaping.
 */
svn_error_t*
svn_io__utf8_to_unicode_longpath(const WCHAR **result,
                                 const char *source,
                                 apr_pool_t *result_pool);
#endif /* WIN32 */

#ifdef __cplusplus
}
#endif /* __cplusplus */


#endif /* SVN_IO_PRIVATE_H */

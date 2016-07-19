# facescrub
This is a small utility that will download the Facescrub dataset to the specified folder.  The resulting images sanitized thusly:
- Only valid jpegs.
- Filenames are normalized in terms of: `gender-image_id-face_id-person_name-(p1xp2xp3xp4).jpg`, where `pn` is one of four corners of the face's bounding box, and gender is either `m` or `f`.

### Usage:
`$ chmod u+x facescrub`

`$ facescrub /image/save/path`

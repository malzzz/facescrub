# facescrub
This is a small utility that will download the Facescrub dataset to the specified folder.  The resulting images are sanitized thusly:
- Only valid jpegs.
- Filenames are normalized in terms of the following components:
  - `gender`, either `m` or `f`
  - `image_id`
  - `face_id`
  - `person_name`, lowercased and separated by an underscore
  - `(p1xp2xp3xp4)`, where `pn` is one of four corners of a face's bounding box
- Components are combined with a `-`; e.g., `1-1-aaron_eckhart-(53x177x418x542).jpg`

When finished, the folder will contain roughly (106,000 - (# of image's that failed to download)) images, totalling approximately 15GB.

### Usage:
`$ chmod +x facescrub`

`$ facescrub /image/save/path`
